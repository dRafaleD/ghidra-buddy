package ghidracompanion.service;

import docking.ComponentProvider;
import ghidra.framework.plugintool.PluginTool;
import java.util.function.Consumer;

/**
 * Manages reversible workspace mode switching (Beginner vs Advanced)
 * without modifying program database state or affecting undo/redo stacks.
 */
public class WorkspaceManager {

    public enum Mode {
        BEGINNER("Guided Mode", "Streamlined workspace with contextual assistance and guided analysis workflows."),
        ADVANCED("Full Mode", "Complete Ghidra workspace with direct access to advanced analysis tools.");

        private final String title;
        private final String description;

        Mode(String title, String description) {
            this.title = title;
            this.description = description;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }
    }

    private final PluginTool tool;
    private Mode currentMode = Mode.BEGINNER;
    private Consumer<Mode> modeChangeListener;

    public WorkspaceManager(PluginTool tool) {
        this.tool = tool;
    }

    public Mode getCurrentMode() {
        return currentMode;
    }

    public void setModeChangeListener(Consumer<Mode> listener) {
        this.modeChangeListener = listener;
    }

    public void removeModeChangeListener(Consumer<Mode> listener) {
        if (this.modeChangeListener == listener) {
            this.modeChangeListener = null;
        }
    }

    /**
     * Toggles between Beginner and Advanced workspace modes.
     * Guaranteed to preserve 100% of the program's analysis, symbols, comments, and project data.
     */
    public void toggleMode(ComponentProvider assistantProvider) {
        setMode(currentMode == Mode.BEGINNER ? Mode.ADVANCED : Mode.BEGINNER, assistantProvider);
    }

    public void setMode(Mode newMode, ComponentProvider assistantProvider) {
        if (newMode == null || newMode == this.currentMode) {
            return;
        }

        this.currentMode = newMode;

        // Apply UI preset adjustments without altering program database
        if (assistantProvider != null && tool != null) {
            if (currentMode == Mode.BEGINNER) {
                tool.showComponentProvider(assistantProvider, true);
                tool.toFront(assistantProvider);
            }
        }

        if (modeChangeListener != null) {
            modeChangeListener.accept(this.currentMode);
        }
    }
}
