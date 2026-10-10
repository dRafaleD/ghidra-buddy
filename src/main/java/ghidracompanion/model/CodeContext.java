package ghidracompanion.model;

import ghidra.program.model.address.Address;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates contextual insights about the currently inspected element in Ghidra.
 */
public class CodeContext {

    public enum ContextType {
        FUNCTION_DEF("Function Definition"),
        IMPORT_CALL("Imported API / Library Call"),
        INTERNAL_CALL("Internal Function Call"),
        STACK_VARIABLE("Stack Variable"),
        STRING_LITERAL("String Literal"),
        BRANCH_INSTRUCTION("Control Flow / Branch"),
        DATA_REFERENCE("Data Reference"),
        DECOMPILER_TOKEN("Decompiler Expression"),
        GENERIC_INSTRUCTION("Instruction"),
        GENERAL("General Analysis Location");

        private final String label;

        ContextType(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        // Backward compatibility
        public String getEnLabel() {
            return label;
        }

        public String getTrLabel() {
            return label;
        }
    }

    public static class ActionSuggestion {
        private final String label;
        private final String actionId;
        private final Address targetAddress;

        public ActionSuggestion(String label, String actionId, Address targetAddress) {
            this.label = label;
            this.actionId = actionId;
            this.targetAddress = targetAddress;
        }

        public String getLabel() {
            return label;
        }

        public String getActionId() {
            return actionId;
        }

        public Address getTargetAddress() {
            return targetAddress;
        }
    }

    private final Address address;
    private final String itemName;
    private final ContextType contextType;
    private final String whatItIs;
    private final String whyItMatters;
    private final String whatToInspectNext;
    private final List<ActionSuggestion> suggestions;

    public CodeContext(Address address,
                       String itemName,
                       ContextType contextType,
                       String whatItIs,
                       String whyItMatters,
                       String whatToInspectNext,
                       List<ActionSuggestion> suggestions) {
        this.address = address;
        this.itemName = itemName != null ? itemName : "";
        this.contextType = contextType != null ? contextType : ContextType.GENERAL;
        this.whatItIs = whatItIs != null ? whatItIs : "";
        this.whyItMatters = whyItMatters != null ? whyItMatters : "";
        this.whatToInspectNext = whatToInspectNext != null ? whatToInspectNext : "";
        this.suggestions = suggestions != null ? new ArrayList<>(suggestions) : Collections.emptyList();
    }

    public Address getAddress() {
        return address;
    }

    public String getItemName() {
        return itemName;
    }

    public ContextType getContextType() {
        return contextType;
    }

    public String getWhatItIs() {
        return whatItIs;
    }

    public String getWhyItMatters() {
        return whyItMatters;
    }

    public String getWhatToInspectNext() {
        return whatToInspectNext;
    }

    public List<ActionSuggestion> getSuggestions() {
        return Collections.unmodifiableList(suggestions);
    }

    public static CodeContext empty(Address address) {
        return new CodeContext(
            address,
            "No Location Selected",
            ContextType.GENERAL,
            "No function, instruction, or data symbol is currently selected.",
            "Select an instruction in the Listing or a token in the Decompiler to inspect its semantics.",
            "To begin tracing program flow, inspect the 'entry' or 'main' function in the Symbol Tree.",
            Collections.emptyList()
        );
    }
}
