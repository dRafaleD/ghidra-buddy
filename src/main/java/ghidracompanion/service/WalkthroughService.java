package ghidracompanion.service;

import ghidracompanion.model.WalkthroughStep;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages the interactive beginner guided walkthrough workflow.
 */
public class WalkthroughService {

    private final List<WalkthroughStep> steps = new ArrayList<>();

    public WalkthroughService() {
        populateDefaultWorkflow();
    }

    private void populateDefaultWorkflow() {
        steps.add(new WalkthroughStep(
            1,
            "1. Binary Ingestion & Auto-Analysis",
            "When opening the target binary in Ghidra CodeBrowser, accept and run the Auto-Analysis suite.",
            "Tip: Monitor the lower right progress bar until background analysis reaches 100% completion."
        ));

        steps.add(new WalkthroughStep(
            2,
            "2. Locate Entry Point or Main Function",
            "In the Symbol Tree navigation panel under Functions, locate and double-click 'entry' or 'main'.",
            "Tip: C executables initialize runtime CRT stubs inside 'entry' prior to transferring control flow to 'main'."
        ));

        steps.add(new WalkthroughStep(
            3,
            "3. Triage Defined Strings & Cross-References",
            "Open Window > Defined Strings to inspect embedded string constants. Select any string of interest and press 'X' to trace callers.",
            "Tip: Tracking cross-references to success/error console banners quickly surfaces the critical validation routine."
        ));

        steps.add(new WalkthroughStep(
            4,
            "4. Correlate Assembly and Decompiler Views",
            "Select an instruction in the Listing view and observe the corresponding C pseudo-code highlighted in the Decompiler view.",
            "Tip: The decompiler provides high-level structure, but CPU flags and signedness checks should be verified against CMP/TEST assembly mnemonics."
        ));

        steps.add(new WalkthroughStep(
            5,
            "5. Symbol Annotation & Renaming",
            "Select undefined variables or subroutines and press 'L' to assign descriptive names. Press ';' to append inline comments.",
            "Tip: Renaming variables propagates across the Ghidra program database, instantly increasing decompiler readability."
        ));
    }

    public List<WalkthroughStep> getSteps() {
        return Collections.unmodifiableList(steps);
    }

    public void setStepCompleted(int stepNumber, boolean completed) {
        for (WalkthroughStep step : steps) {
            if (step.getStepNumber() == stepNumber) {
                step.setCompleted(completed);
                break;
            }
        }
    }

    public void resetProgress() {
        for (WalkthroughStep step : steps) {
            step.setCompleted(false);
        }
    }

    public int getCompletedCount() {
        int count = 0;
        for (WalkthroughStep step : steps) {
            if (step.isCompleted()) {
                count++;
            }
        }
        return count;
    }
}
