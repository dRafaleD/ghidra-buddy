package ghidracompanion.model;

/**
 * Represents a single milestone step in the beginner analysis workflow.
 */
public class WalkthroughStep {
    private final int stepNumber;
    private final String title;
    private final String description;
    private final String targetHint;
    private boolean completed;

    public WalkthroughStep(int stepNumber, String title, String description, String targetHint) {
        this.stepNumber = stepNumber;
        this.title = title;
        this.description = description;
        this.targetHint = targetHint;
        this.completed = false;
    }

    public int getStepNumber() {
        return stepNumber;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getTargetHint() {
        return targetHint;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
