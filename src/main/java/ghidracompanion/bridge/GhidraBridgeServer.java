package ghidracompanion.bridge;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.app.decompiler.DecompiledFunction;
import ghidra.app.services.GoToService;
import ghidra.framework.plugintool.PluginTool;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressFactory;
import ghidra.program.model.data.StringDataInstance;
import ghidra.program.model.listing.*;
import ghidra.program.model.symbol.ReferenceIterator;
import ghidra.program.util.ProgramLocation;
import ghidra.util.task.TaskMonitor;
import ghidracompanion.model.CodeContext;
import ghidracompanion.model.GlossaryEntry;
import ghidracompanion.model.WalkthroughStep;
import ghidracompanion.service.ContextAnalysisService;
import ghidracompanion.service.GlossaryService;
import ghidracompanion.service.WalkthroughService;
import ghidracompanion.service.WorkspaceManager;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * Loopback HTTP/JSON-RPC Bridge server exposing Ghidra's program analysis,
 * decompilation, and educational companion intelligence to modern external frontends.
 */
public class GhidraBridgeServer {

    private final int port;
    private final PluginTool tool;
    private final ContextAnalysisService contextAnalysisService;
    private final GlossaryService glossaryService;
    private final WalkthroughService walkthroughService;
    private final WorkspaceManager workspaceManager;

    private HttpServer server;
    private volatile Program currentProgram;
    private volatile DecompInterface decompiler;

    public GhidraBridgeServer(int port,
                              PluginTool tool,
                              ContextAnalysisService contextAnalysisService,
                              GlossaryService glossaryService,
                              WalkthroughService walkthroughService,
                              WorkspaceManager workspaceManager) {
        this.port = port;
        this.tool = tool;
        this.contextAnalysisService = contextAnalysisService;
        this.glossaryService = glossaryService;
        this.walkthroughService = walkthroughService;
        this.workspaceManager = workspaceManager;
    }

    public synchronized void setCurrentProgram(Program program) {
        this.currentProgram = program;
        disposeDecompiler();
        if (program != null) {
            decompiler = new DecompInterface();
            decompiler.openProgram(program);
        }
    }

    public synchronized void start() throws IOException {
        if (server != null) return;

        server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.setExecutor(Executors.newCachedThreadPool());

        server.createContext("/api/status", safe(new StatusHandler()));
        server.createContext("/api/functions", safe(new FunctionsHandler()));
        server.createContext("/api/function", safe(new FunctionDetailHandler()));
        server.createContext("/api/disassembly", safe(new DisassemblyHandler()));
        server.createContext("/api/decompile", safe(new DecompileHandler()));
        server.createContext("/api/strings", safe(new StringsHandler()));
        server.createContext("/api/analyze-context", safe(new AnalyzeContextHandler()));
        server.createContext("/api/glossary", safe(new GlossaryHandler()));
        server.createContext("/api/walkthrough", safe(new WalkthroughHandler()));
        server.createContext("/api/navigate", safe(new NavigateHandler()));

        server.start();
    }

    private void disposeDecompiler() {
        DecompInterface old = decompiler;
        decompiler = null;
        if (old != null) {
            synchronized (old) {
                old.dispose();
            }
        }
    }

    public synchronized void stop() {
        disposeDecompiler();
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    public int getPort() {
        return port;
    }

    // ----------------- Handlers -----------------

    private class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCors(exchange)) return;
            final Program currentProgram = GhidraBridgeServer.this.currentProgram;

            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("running", true);
            resp.put("ghidraVersion", "12.1.3");
            resp.put("mode", workspaceManager.getCurrentMode().name());

            if (currentProgram != null) {
                Map<String, Object> prog = new LinkedHashMap<>();
                prog.put("name", currentProgram.getName());
                prog.put("executablePath", currentProgram.getExecutablePath());
                prog.put("language", currentProgram.getLanguageID().getIdAsString());
                prog.put("compiler", currentProgram.getCompilerSpec().getCompilerSpecID().getIdAsString());
                prog.put("minAddress", currentProgram.getMinAddress().toString());
                prog.put("maxAddress", currentProgram.getMaxAddress().toString());
                prog.put("creationDate", currentProgram.getCreationDate().toString());
                resp.put("program", prog);
            } else {
                resp.put("program", null);
            }

            sendJsonResponse(exchange, 200, JsonHelper.mapToJson(resp));
        }
    }

    private class FunctionsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCors(exchange)) return;
            final Program currentProgram = GhidraBridgeServer.this.currentProgram;

            if (currentProgram == null) {
                sendJsonResponse(exchange, 200, "[]");
                return;
            }

            List<Map<String, Object>> list = new ArrayList<>();
            FunctionIterator iter = currentProgram.getFunctionManager().getFunctions(true);
            int count = 0;
            while (iter.hasNext() && count < 500) {
                Function f = iter.next();
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("name", f.getName());
                m.put("entryPoint", f.getEntryPoint().toString());
                m.put("paramCount", f.getParameterCount());
                m.put("returnType", f.getReturnType() != null ? f.getReturnType().getDisplayName() : "void");
                m.put("isExternal", f.isExternal() || f.isThunk());
                m.put("size", f.getBody().getNumAddresses());
                list.add(m);
                count++;
            }

            sendJsonResponse(exchange, 200, JsonHelper.listToJson(list));
        }
    }

    private class FunctionDetailHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCors(exchange)) return;
            final Program currentProgram = GhidraBridgeServer.this.currentProgram;

            if (currentProgram == null) {
                sendJsonResponse(exchange, 400, "{\"error\":\"No active program\"}");
                return;
            }

            Map<String, String> query = parseQuery(exchange.getRequestURI());
            String addrStr = query.get("address");
            String nameStr = query.get("name");

            Function func = null;
            if (addrStr != null) {
                Address addr = parseAddress(currentProgram, addrStr);
                if (addr != null) {
                    func = currentProgram.getFunctionManager().getFunctionAt(addr);
                    if (func == null) {
                        func = currentProgram.getFunctionManager().getFunctionContaining(addr);
                    }
                }
            } else if (nameStr != null) {
                FunctionIterator it = currentProgram.getFunctionManager().getFunctions(true);
                while (it.hasNext()) {
                    Function f = it.next();
                    if (f.getName().equals(nameStr)) {
                        func = f;
                        break;
                    }
                }
            }

            if (func == null) {
                sendJsonResponse(exchange, 404, "{\"error\":\"Function not found\"}");
                return;
            }

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", func.getName());
            m.put("entryPoint", func.getEntryPoint().toString());
            m.put("prototype", func.getPrototypeString(true, false));
            m.put("callingConvention", func.getCallingConventionName());
            m.put("parameterCount", func.getParameterCount());
            m.put("returnType", func.getReturnType() != null ? func.getReturnType().getDisplayName() : "void");
            m.put("stackPurge", func.getStackPurgeSize());
            m.put("isExternal", func.isExternal());

            sendJsonResponse(exchange, 200, JsonHelper.mapToJson(m));
        }
    }

    private class DisassemblyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCors(exchange)) return;
            final Program currentProgram = GhidraBridgeServer.this.currentProgram;

            if (currentProgram == null) {
                sendJsonResponse(exchange, 200, "[]");
                return;
            }

            Map<String, String> query = parseQuery(exchange.getRequestURI());
            String addrStr = query.get("address");
            int limit = 50;
            if (query.containsKey("count")) {
                try {
                    limit = Math.min(200, Math.max(1, Integer.parseInt(query.get("count"))));
                } catch (NumberFormatException ignored) {}
            }

            Address startAddr = addrStr != null ? parseAddress(currentProgram, addrStr) : currentProgram.getMinAddress();
            if (startAddr == null) {
                startAddr = currentProgram.getMinAddress();
            }

            InstructionIterator iter = currentProgram.getListing().getInstructions(startAddr, true);
            List<Map<String, Object>> instructions = new ArrayList<>();
            int count = 0;

            while (iter.hasNext() && count < limit) {
                Instruction inst = iter.next();
                Map<String, Object> im = new LinkedHashMap<>();
                im.put("address", inst.getAddress().toString());
                im.put("mnemonic", inst.getMnemonicString());
                im.put("operands", getOperandsRepresentation(inst));
                String hexBytes = "";
                try {
                    hexBytes = bytesToHex(inst.getBytes());
                } catch (Exception ignored) {}

                String comment = null;
                try {
                    comment = inst.getComment(CommentType.EOL);
                } catch (Exception ignored) {}

                im.put("bytes", hexBytes);
                im.put("comment", comment);
                im.put("isCall", inst.getMnemonicString().toUpperCase().startsWith("CALL"));
                im.put("isBranch", inst.getFlowType().isJump() || inst.getFlowType().isConditional());
                instructions.add(im);
                count++;
            }

            sendJsonResponse(exchange, 200, JsonHelper.listToJson(instructions));
        }
    }

    private class DecompileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCors(exchange)) return;
            final Program currentProgram = GhidraBridgeServer.this.currentProgram;

            final DecompInterface decompiler = GhidraBridgeServer.this.decompiler;
            if (currentProgram == null || decompiler == null) {
                sendJsonResponse(exchange, 400, "{\"error\":\"No active program or decompiler\"}");
                return;
            }

            Map<String, String> query = parseQuery(exchange.getRequestURI());
            String addrStr = query.get("address");
            Address addr = addrStr != null ? parseAddress(currentProgram, addrStr) : null;

            Function func = null;
            if (addr != null) {
                func = currentProgram.getFunctionManager().getFunctionAt(addr);
                if (func == null) {
                    func = currentProgram.getFunctionManager().getFunctionContaining(addr);
                }
            }

            if (func == null) {
                sendJsonResponse(exchange, 404, "{\"error\":\"Function not found at specified address\"}");
                return;
            }

            DecompileResults results;
            synchronized (decompiler) {
                results = decompiler.decompileFunction(func, 30, TaskMonitor.DUMMY);
            }
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("functionName", func.getName());
            resp.put("entryPoint", func.getEntryPoint().toString());

            if (results != null && results.decompileCompleted()) {
                DecompiledFunction decompFunc = results.getDecompiledFunction();
                resp.put("success", true);
                resp.put("signature", decompFunc.getSignature());
                resp.put("cCode", decompFunc.getC());
            } else {
                resp.put("success", false);
                resp.put("errorMessage", results != null ? results.getErrorMessage() : "Decompilation failed");
            }

            sendJsonResponse(exchange, 200, JsonHelper.mapToJson(resp));
        }
    }

    private class StringsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCors(exchange)) return;
            final Program currentProgram = GhidraBridgeServer.this.currentProgram;

            if (currentProgram == null) {
                sendJsonResponse(exchange, 200, "[]");
                return;
            }

            List<Map<String, Object>> list = new ArrayList<>();
            DataIterator it = currentProgram.getListing().getDefinedData(true);
            int count = 0;

            while (it.hasNext() && count < 300) {
                Data data = it.next();
                if (data.hasStringValue()) {
                    StringDataInstance sdi = StringDataInstance.getStringDataInstance(data);
                    String val = sdi != null ? sdi.getStringValue() : "";
                    if (val != null && !val.trim().isEmpty()) {
                        int refCount = 0;
                        ReferenceIterator refIt = currentProgram.getReferenceManager().getReferencesTo(data.getAddress());
                        while (refIt.hasNext()) {
                            refIt.next();
                            refCount++;
                        }

                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("address", data.getAddress().toString());
                        m.put("value", val);
                        m.put("length", data.getLength());
                        m.put("xrefCount", refCount);
                        list.add(m);
                        count++;
                    }
                }
            }

            sendJsonResponse(exchange, 200, JsonHelper.listToJson(list));
        }
    }

    private class AnalyzeContextHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCors(exchange)) return;
            final Program currentProgram = GhidraBridgeServer.this.currentProgram;

            if (currentProgram == null) {
                sendJsonResponse(exchange, 200, "{}");
                return;
            }

            Map<String, String> query = parseQuery(exchange.getRequestURI());
            String addrStr = query.get("address");
            Address addr = addrStr != null ? parseAddress(currentProgram, addrStr) : null;
            if (addr == null) {
                addr = currentProgram.getMinAddress();
            }

            ProgramLocation loc = new ProgramLocation(currentProgram, addr);
            CodeContext ctx = contextAnalysisService.analyzeLocation(currentProgram, loc);

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("address", ctx.getAddress() != null ? ctx.getAddress().toString() : "-");
            m.put("itemName", ctx.getItemName());
            m.put("contextType", ctx.getContextType().name());
            m.put("typeLabel", ctx.getContextType().getLabel());
            m.put("typeLabelEn", ctx.getContextType().getLabel());
            m.put("whatItIs", ctx.getWhatItIs());
            m.put("whyItMatters", ctx.getWhyItMatters());
            m.put("whatToInspectNext", ctx.getWhatToInspectNext());

            List<Map<String, String>> actions = new ArrayList<>();
            for (CodeContext.ActionSuggestion a : ctx.getSuggestions()) {
                Map<String, String> am = new LinkedHashMap<>();
                am.put("label", a.getLabel());
                am.put("actionId", a.getActionId());
                am.put("targetAddress", a.getTargetAddress() != null ? a.getTargetAddress().toString() : "");
                actions.add(am);
            }
            m.put("suggestions", actions);

            sendJsonResponse(exchange, 200, JsonHelper.mapToJson(m));
        }
    }

    private class GlossaryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCors(exchange)) return;

            Map<String, String> query = parseQuery(exchange.getRequestURI());
            String q = query.get("q");

            List<GlossaryEntry> entries = glossaryService.search(q);
            List<Map<String, String>> list = new ArrayList<>();

            for (GlossaryEntry e : entries) {
                Map<String, String> m = new LinkedHashMap<>();
                m.put("term", e.getTerm());
                m.put("category", e.getCategory());
                m.put("summary", e.getSummary());
                m.put("detailedDescription", e.getDetailedDescription());
                m.put("beginnerTip", e.getBeginnerTip());
                list.add(m);
            }

            sendJsonResponse(exchange, 200, JsonHelper.listToJson(list));
        }
    }

    private class WalkthroughHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCors(exchange)) return;

            List<WalkthroughStep> steps = walkthroughService.getSteps();
            List<Map<String, Object>> list = new ArrayList<>();

            for (WalkthroughStep s : steps) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("stepNumber", s.getStepNumber());
                m.put("title", s.getTitle());
                m.put("description", s.getDescription());
                m.put("targetHint", s.getTargetHint());
                m.put("completed", s.isCompleted());
                list.add(m);
            }

            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("completedCount", walkthroughService.getCompletedCount());
            resp.put("totalCount", steps.size());
            resp.put("steps", list);

            sendJsonResponse(exchange, 200, JsonHelper.mapToJson(resp));
        }
    }

    private class NavigateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCors(exchange)) return;
            final Program currentProgram = GhidraBridgeServer.this.currentProgram;

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, "{\"error\":\"Use POST\"}");
                return;
            }

            Map<String, String> query = parseQuery(exchange.getRequestURI());
            String addrStr = query.get("address");

            if (addrStr != null && tool != null && currentProgram != null) {
                Address target = parseAddress(currentProgram, addrStr);
                if (target != null) {
                    GoToService gotoService = tool.getService(GoToService.class);
                    if (gotoService != null) {
                        gotoService.goTo(target);
                        sendJsonResponse(exchange, 200, "{\"success\":true}");
                        return;
                    }
                }
            }
            sendJsonResponse(exchange, 400, "{\"success\":false}");
        }
    }

    // ----------------- Helpers -----------------

    private HttpHandler safe(HttpHandler delegate) {
        return exchange -> {
            try {
                delegate.handle(exchange);
            } catch (Exception e) {
                try {
                    sendJsonResponse(exchange, 500, "{\"error\":\"Internal error: program may have been closed\"}");
                } catch (Exception ignored) {
                    exchange.close();
                }
            }
        };
    }

    /** Origins allowed to call the bridge from a browser context (Tauri app and Vite dev server). */
    private static final Set<String> ALLOWED_ORIGINS = Set.of(
        "http://localhost:5173",
        "http://127.0.0.1:5173",
        "tauri://localhost",
        "http://tauri.localhost",
        "https://tauri.localhost"
    );

    private boolean rejectUntrusted(HttpExchange exchange) throws IOException {
        // Block DNS-rebinding: the Host header must be the loopback address we bound to.
        String host = exchange.getRequestHeaders().getFirst("Host");
        boolean hostOk = host != null
            && (host.equals("127.0.0.1:" + port) || host.equals("localhost:" + port));
        // Block cross-site browser calls: any Origin that is present must be allow-listed.
        String origin = exchange.getRequestHeaders().getFirst("Origin");
        boolean originOk = origin == null || ALLOWED_ORIGINS.contains(origin);
        if (hostOk && originOk) {
            return false;
        }
        byte[] body = "{\"error\":\"Forbidden\"}".getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(403, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
        return true;
    }

    private boolean handleCors(HttpExchange exchange) throws IOException {
        if (rejectUntrusted(exchange)) return true;

        String origin = exchange.getRequestHeaders().getFirst("Origin");
        if (origin != null) {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", origin);
            exchange.getResponseHeaders().set("Vary", "Origin");
        }
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return true;
        }
        return false;
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private Map<String, String> parseQuery(URI uri) {
        Map<String, String> map = new HashMap<>();
        String rawQuery = uri.getQuery();
        if (rawQuery == null || rawQuery.isEmpty()) return map;

        for (String pair : rawQuery.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                map.put(kv[0], kv[1]);
            } else if (kv.length == 1) {
                map.put(kv[0], "");
            }
        }
        return map;
    }

    private Address parseAddress(Program currentProgram, String addrStr) {
        if (addrStr == null || currentProgram == null) return null;
        try {
            AddressFactory factory = currentProgram.getAddressFactory();
            return factory.getAddress(addrStr);
        } catch (Exception e) {
            return null;
        }
    }

    private String getOperandsRepresentation(Instruction inst) {
        int numOps = inst.getNumOperands();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numOps; i++) {
            if (i > 0) sb.append(", ");
            sb.append(inst.getDefaultOperandRepresentation(i));
        }
        return sb.toString();
    }

    private String bytesToHex(byte[] bytes) {
        if (bytes == null) return "";
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }
}
