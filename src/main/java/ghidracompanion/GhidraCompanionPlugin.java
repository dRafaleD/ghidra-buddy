package ghidracompanion;

import ghidra.app.plugin.PluginCategoryNames;
import ghidra.app.plugin.ProgramPlugin;
import ghidra.framework.plugintool.PluginInfo;
import ghidra.framework.plugintool.PluginTool;
import ghidra.framework.plugintool.util.PluginStatus;
import ghidra.program.model.listing.Program;
import ghidra.program.util.ProgramLocation;
import ghidra.util.Msg;
import ghidracompanion.bridge.GhidraBridgeServer;
import ghidracompanion.model.CodeContext;
import ghidracompanion.service.ContextAnalysisService;
import ghidracompanion.service.GlossaryService;
import ghidracompanion.service.WalkthroughService;
import ghidracompanion.service.WorkspaceManager;
import ghidracompanion.ui.LearningAssistantProvider;

import javax.swing.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main Ghidra plugin entry point for Ghidra Companion.
 * Connects Ghidra's program events with the educational learning assistant UI
 * and serves the JSON-RPC bridge for external modern frontends.
 */
@PluginInfo(
    status = PluginStatus.STABLE,
    packageName = "GhidraCompanion",
    category = PluginCategoryNames.COMMON,
    shortDescription = "Approachable learning companion and contextual guide for Ghidra.",
    description = "Provides real-time contextual explanations, beginner checklists, and an RE glossary inside Ghidra."
)
public class GhidraCompanionPlugin extends ProgramPlugin {

    private final LearningAssistantProvider provider;
    private final WorkspaceManager workspaceManager;
    private final ContextAnalysisService contextAnalysisService;
    private final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();
    private static final int DEFAULT_BRIDGE_PORT = 19842;
    private GhidraBridgeServer bridgeServer;

    public GhidraCompanionPlugin(PluginTool tool) {
        super(tool);
        this.workspaceManager = new WorkspaceManager(tool);
        this.contextAnalysisService = new ContextAnalysisService();
        this.provider = new LearningAssistantProvider(this, getName(), workspaceManager);

        int port = Integer.getInteger("ghidracompanion.port", DEFAULT_BRIDGE_PORT);
        try {
            this.bridgeServer = new GhidraBridgeServer(
                port,
                tool,
                contextAnalysisService,
                new GlossaryService(),
                new WalkthroughService(),
                workspaceManager
            );
            this.bridgeServer.start();
        } catch (Exception e) {
            this.bridgeServer = null;
            Msg.warn(this, "Ghidra Companion bridge could not start on port " + port
                + " (" + e.getMessage() + "). The learning panel still works; "
                + "use -Dghidracompanion.port=<port> to pick another port.");
        }
    }

    @Override
    protected void programActivated(Program program) {
        super.programActivated(program);
        if (bridgeServer != null) {
            bridgeServer.setCurrentProgram(program);
        }
        if (currentLocation != null) {
            triggerContextAnalysis(currentLocation);
        } else if (program != null) {
            triggerContextAnalysis(new ProgramLocation(program, program.getMinAddress()));
        }
    }

    @Override
    protected void programDeactivated(Program program) {
        super.programDeactivated(program);
        if (bridgeServer != null) {
            bridgeServer.setCurrentProgram(null);
        }
        SwingUtilities.invokeLater(() -> provider.updateContext(CodeContext.empty(null)));
    }

    @Override
    protected void locationChanged(ProgramLocation loc) {
        super.locationChanged(loc);
        if (loc != null && currentProgram != null) {
            triggerContextAnalysis(loc);
        }
    }

    private void triggerContextAnalysis(ProgramLocation loc) {
        Program prog = currentProgram;
        if (prog == null || loc == null) {
            return;
        }

        // Analyze off-EDT to guarantee smooth UI responsiveness on large binaries
        backgroundExecutor.submit(() -> {
            try {
                CodeContext context = contextAnalysisService.analyzeLocation(prog, loc);
                SwingUtilities.invokeLater(() -> provider.updateContext(context));
            } catch (Exception e) {
                // Failsafe fallback
                SwingUtilities.invokeLater(() -> provider.updateContext(CodeContext.empty(loc.getAddress())));
            }
        });
    }

    @Override
    public void dispose() {
        backgroundExecutor.shutdownNow();
        if (bridgeServer != null) {
            bridgeServer.stop();
        }
        if (provider != null) {
            provider.close();
        }
        super.dispose();
    }
}
