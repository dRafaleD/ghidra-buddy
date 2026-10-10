package ghidracompanion.service;

import ghidra.program.model.address.Address;
import ghidra.program.model.data.StringDataInstance;
import ghidra.program.model.listing.*;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceIterator;
import ghidra.program.model.symbol.Symbol;
import ghidra.program.util.FunctionLocation;
import ghidra.program.util.ProgramLocation;
import ghidra.program.util.VariableLocation;
import ghidracompanion.model.CodeContext;
import ghidracompanion.model.CodeContext.ActionSuggestion;
import ghidracompanion.model.CodeContext.ContextType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Deterministic, offline analysis engine that inspects Ghidra's program models
 * and generates technically accurate educational explanations.
 */
public class ContextAnalysisService {

    public CodeContext analyzeLocation(Program program, ProgramLocation location) {
        if (program == null || location == null) {
            return CodeContext.empty(null);
        }

        Address address = location.getAddress();
        if (address == null) {
            return CodeContext.empty(null);
        }

        List<ActionSuggestion> suggestions = new ArrayList<>();

        // 1. Variable / Stack Variable Location
        if (location instanceof VariableLocation) {
            VariableLocation varLoc = (VariableLocation) location;
            Variable var = varLoc.getVariable();
            if (var != null) {
                return analyzeVariable(var, address);
            }
        }

        Listing listing = program.getListing();

        // 2. Data / String Inspection
        Data data = listing.getDefinedDataAt(address);
        if (data != null && (data.hasStringValue() || data.isPointer())) {
            CodeContext stringCtx = analyzeData(data, program, suggestions);
            if (stringCtx != null) {
                return stringCtx;
            }
        }

        // 3. Instruction Inspection
        Instruction instruction = listing.getInstructionAt(address);
        if (instruction != null) {
            return analyzeInstruction(instruction, program, suggestions);
        }

        // 4. Function Header Inspection
        Function function = listing.getFunctionAt(address);
        if (function != null) {
            return analyzeFunction(function, program, suggestions);
        }

        // 5. Surrounding Function Context fallback
        Function containingFunction = listing.getFunctionContaining(address);
        if (containingFunction != null) {
            return new CodeContext(
                address,
                containingFunction.getName() + " (Function Body)",
                ContextType.GENERAL,
                "This address is located inside function '" + containingFunction.getName() + "', but no specific instruction or defined data item is currently selected.",
                "Function entry point: " + containingFunction.getEntryPoint() + ". Total size: " + containingFunction.getBody().getNumAddresses() + " bytes.",
                "To inspect individual instructions, select a specific line in the Listing view or examine the Decompiler view.",
                suggestions
            );
        }

        return CodeContext.empty(address);
    }

    private CodeContext analyzeVariable(Variable var, Address address) {
        List<ActionSuggestion> suggestions = new ArrayList<>();
        String varName = var.getName();
        String typeName = var.getDataType() != null ? var.getDataType().getDisplayName() : "Unknown Type";
        int stackOffset = var.getStackOffset();

        String whatItIs = String.format(
            "Local variable: '%s', Type: %s. Stack offset: %s.",
            varName, typeName, (stackOffset != 0 ? "Stack[0x" + Integer.toHexString(stackOffset) + "]" : "Register / Auto")
        );

        String whyItMatters = "Stack variables store local state, intermediate calculation results, and allocated buffers during function execution. "
            + "Properly typing and naming variables significantly improves decompiler fidelity.";

        String whatToInspect = "Press 'L' (Rename) or 'Ctrl+L' (Re-type) to update variable identifiers and data types.";

        return new CodeContext(
            address,
            varName,
            ContextType.STACK_VARIABLE,
            whatItIs,
            whyItMatters,
            whatToInspect,
            suggestions
        );
    }

    private CodeContext analyzeData(Data data, Program program, List<ActionSuggestion> suggestions) {
        String valStr = "";
        StringDataInstance strInstance = StringDataInstance.getStringDataInstance(data);
        if (strInstance != null && strInstance.getStringValue() != null) {
            valStr = strInstance.getStringValue();
        } else if (data.getValue() != null) {
            valStr = data.getValue().toString();
        }

        Address dataAddr = data.getAddress();
        int refCount = 0;
        ReferenceIterator refIter = program.getReferenceManager().getReferencesTo(dataAddr);
        Address firstRefAddr = null;
        while (refIter.hasNext()) {
            Reference ref = refIter.next();
            if (firstRefAddr == null) {
                firstRefAddr = ref.getFromAddress();
            }
            refCount++;
        }

        if (firstRefAddr != null) {
            suggestions.add(new ActionSuggestion("Go to First Reference (" + firstRefAddr + ")", "GOTO", firstRefAddr));
        }

        String whatItIs = String.format("Constant Data / String: \"%s\" (Length: %d bytes).", valStr, data.getLength());
        String whyItMatters = String.format(
            "Referenced by %d cross-reference(s) (XREFs) across the binary. "
                + "String literals frequently reveal status banners, API endpoints, log messages, or configuration keys.",
            refCount
        );
        String whatToInspect = "Press 'X' to inspect cross-references (XREFs) to find referencing code locations.";

        return new CodeContext(
            dataAddr,
            "\"" + (valStr.length() > 30 ? valStr.substring(0, 27) + "..." : valStr) + "\"",
            ContextType.STRING_LITERAL,
            whatItIs,
            whyItMatters,
            whatToInspect,
            suggestions
        );
    }

    private CodeContext analyzeInstruction(Instruction inst, Program program, List<ActionSuggestion> suggestions) {
        String mnemonic = inst.getMnemonicString().toUpperCase(Locale.ROOT);
        Address instAddr = inst.getAddress();

        // Check if instruction is a CALL
        if (mnemonic.startsWith("CALL") || mnemonic.equals("BL") || mnemonic.equals("BLX")) {
            return analyzeCallInstruction(inst, program, suggestions);
        }

        // Check if instruction is a conditional or unconditional branch
        if (isBranchInstruction(mnemonic)) {
            return analyzeBranchInstruction(inst, mnemonic, suggestions);
        }

        // Check if instruction is stack frame setup (prologue/epilogue)
        if (mnemonic.equals("PUSH") || mnemonic.equals("POP") || mnemonic.equals("SUB") || mnemonic.equals("ADD")) {
            String op0 = inst.getDefaultOperandRepresentation(0);
            if (op0 != null && (op0.contains("SP") || op0.contains("BP") || op0.contains("ESP") || op0.contains("RSP"))) {
                return new CodeContext(
                    instAddr,
                    inst.toString(),
                    ContextType.GENERIC_INSTRUCTION,
                    "Stack Frame Management (Setup / Cleanup): '" + inst + "'.",
                    "This instruction allocates stack space for local variables or restores prior processor state upon return.",
                    "Inspect the Stack Variables listing to review variable allocations for this frame.",
                    suggestions
                );
            }
        }

        // Comparison instruction (CMP / TEST)
        if (mnemonic.equals("CMP") || mnemonic.equals("TEST")) {
            return new CodeContext(
                instAddr,
                inst.toString(),
                ContextType.BRANCH_INSTRUCTION,
                "Comparison Instruction: '" + inst + "'.",
                "Evaluates operands and updates processor status flags (ZF, SF, CF, OF). The subsequent conditional jump evaluates these flags to determine control flow.",
                "Examine the conditional jump instruction immediately following this comparison to identify decision logic.",
                suggestions
            );
        }

        return new CodeContext(
            instAddr,
            inst.toString(),
            ContextType.GENERIC_INSTRUCTION,
            "Machine Instruction: '" + inst + "'.",
            "Mnemonic: " + mnemonic + ". Standard processor instruction.",
            "Review the corresponding pseudo-code expression in the Decompiler view.",
            suggestions
        );
    }

    private CodeContext analyzeCallInstruction(Instruction inst, Program program, List<ActionSuggestion> suggestions) {
        Address instAddr = inst.getAddress();
        Address[] flowAddrs = inst.getFlows();
        Address targetAddr = (flowAddrs != null && flowAddrs.length > 0) ? flowAddrs[0] : null;

        if (targetAddr != null) {
            suggestions.add(new ActionSuggestion("Navigate to Target (" + targetAddr + ")", "GOTO", targetAddr));

            Function targetFunc = program.getFunctionManager().getFunctionAt(targetAddr);
            if (targetFunc != null) {
                boolean isThunkOrExternal = targetFunc.isThunk() || targetFunc.isExternal();
                String targetName = targetFunc.getName();

                if (isThunkOrExternal) {
                    return new CodeContext(
                        instAddr,
                        "API / External Import Call: " + targetName,
                        ContextType.IMPORT_CALL,
                        "Call to external shared library or system API: '" + targetName + "'.",
                        "The program transfers execution to an external library function. "
                            + "API calls provide high-value signals for filesystem, networking, registry, and memory behavior.",
                        "Inspect calling arguments in parameter registers (e.g. RCX, RDX, R8, R9 for x64 Windows ABI) or stack pushes.",
                        suggestions
                    );
                } else {
                    return new CodeContext(
                        instAddr,
                        "Internal Function Call: " + targetName,
                        ContextType.INTERNAL_CALL,
                        "Call to internal binary function: '" + targetName + "'.",
                        "Signature: " + targetFunc.getPrototypeString(true, false) + ". "
                            + "Parameter Count: " + targetFunc.getParameterCount() + ".",
                        "Double-click the target address or click 'Navigate to Target' to inspect subroutine implementation.",
                        suggestions
                    );
                }
            }
        }

        return new CodeContext(
            instAddr,
            inst.toString(),
            ContextType.INTERNAL_CALL,
            "Subroutine Invocation: '" + inst + "'.",
            "Control flow transfers to target address and resumes execution at the next instruction upon return (RET).",
            "Verify calling convention and arguments passed at call site.",
            suggestions
        );
    }

    private CodeContext analyzeBranchInstruction(Instruction inst, String mnemonic, List<ActionSuggestion> suggestions) {
        Address instAddr = inst.getAddress();
        Address[] flows = inst.getFlows();
        Address targetAddr = (flows != null && flows.length > 0) ? flows[0] : null;

        if (targetAddr != null) {
            suggestions.add(new ActionSuggestion("Jump to Target (" + targetAddr + ")", "GOTO", targetAddr));
        }

        String conditionMeaning = getBranchConditionMeaning(mnemonic);

        return new CodeContext(
            instAddr,
            inst.toString(),
            ContextType.BRANCH_INSTRUCTION,
            "Branch Instruction: '" + mnemonic + "'. " + conditionMeaning,
            "Directs execution flow. Implements conditional logic (if/else), switch structures, or loops (for/while).",
            "Inspect preceding comparison (CMP/TEST) instructions and the target basic block.",
            suggestions
        );
    }

    private CodeContext analyzeFunction(Function func, Program program, List<ActionSuggestion> suggestions) {
        Address entry = func.getEntryPoint();
        String funcName = func.getName();
        String prototype = func.getPrototypeString(true, false);
        String callingConv = func.getCallingConventionName();
        if (callingConv == null || callingConv.isEmpty()) {
            callingConv = "Default / Inferred";
        }

        int refCount = func.getSymbol().getReferenceCount();
        String whatItIs = String.format("Function Definition: %s. Calling Convention: %s.", prototype, callingConv);
        String whyItMatters = String.format(
            "Referenced by %d call site(s) (XREFs). "
                + "Entry point: %s. Parameter count: %d.",
            refCount, entry, func.getParameterCount()
        );
        String whatToInspect = "Press 'X' to trace callers. Review decompiled C pseudo-code in the Decompiler view.";

        return new CodeContext(
            entry,
            funcName,
            ContextType.FUNCTION_DEF,
            whatItIs,
            whyItMatters,
            whatToInspect,
            suggestions
        );
    }

    private boolean isBranchInstruction(String mnemonic) {
        return mnemonic.startsWith("J") || mnemonic.startsWith("B") || mnemonic.equals("LOOP");
    }

    private String getBranchConditionMeaning(String m) {
        switch (m) {
            case "JZ":
            case "JE":
                return "Branches if Zero Flag is set (ZF=1), indicating equality (e.g. if (a == b)).";
            case "JNZ":
            case "JNE":
                return "Branches if Zero Flag is clear (ZF=0), indicating inequality (e.g. if (a != b)).";
            case "JG":
            case "JNLE":
                return "Branches if greater than (signed comparison, ZF=0 and SF=OF).";
            case "JL":
            case "JNGE":
                return "Branches if less than (signed comparison, SF!=OF).";
            case "JA":
            case "JNBE":
                return "Branches if above (unsigned comparison, CF=0 and ZF=0).";
            case "JB":
            case "JNAE":
                return "Branches if below (unsigned comparison, CF=1).";
            case "JMP":
            case "B":
                return "Unconditional branch to target address.";
            default:
                return "Conditional control-flow transfer.";
        }
    }
}
