package ghidracompanion.model;

/**
 * Represents a single reverse engineering and Ghidra educational glossary concept.
 */
public class GlossaryEntry {
    private final String term;
    private final String category;
    private final String summary;
    private final String detailedDescription;
    private final String beginnerTip;

    public GlossaryEntry(String term, String category, String summary, String detailedDescription, String beginnerTip) {
        this.term = term;
        this.category = category;
        this.summary = summary;
        this.detailedDescription = detailedDescription;
        this.beginnerTip = beginnerTip;
    }

    public String getTerm() {
        return term;
    }

    public String getCategory() {
        return category;
    }

    public String getSummary() {
        return summary;
    }

    public String getDetailedDescription() {
        return detailedDescription;
    }

    public String getBeginnerTip() {
        return beginnerTip;
    }

    @Override
    public String toString() {
        return term + " [" + category + "]";
    }
}
