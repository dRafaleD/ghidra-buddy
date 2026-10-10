package ghidracompanion;

import ghidracompanion.model.GlossaryEntry;
import ghidracompanion.service.GlossaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GlossaryServiceTest {

    private GlossaryService glossaryService;

    @BeforeEach
    public void setUp() {
        glossaryService = new GlossaryService();
    }

    @Test
    public void testDefaultEntriesLoaded() {
        List<GlossaryEntry> entries = glossaryService.getAllEntries();
        assertNotNull(entries);
        assertFalse(entries.isEmpty(), "Glossary must contain default entries.");
        assertTrue(entries.size() >= 5, "At least 5 core concepts must be defined.");
    }

    @Test
    public void testSearchByKeyword() {
        List<GlossaryEntry> results = glossaryService.search("XREF");
        assertFalse(results.isEmpty(), "Query for 'XREF' must return results.");
        assertTrue(results.get(0).getTerm().contains("XREF"));

        List<GlossaryEntry> decompilerResults = glossaryService.search("Decompiler");
        assertFalse(decompilerResults.isEmpty(), "Query for 'Decompiler' must return results.");
    }

    @Test
    public void testSearchEmptyReturnsAll() {
        List<GlossaryEntry> all = glossaryService.getAllEntries();
        List<GlossaryEntry> searchEmpty = glossaryService.search("");
        assertEquals(all.size(), searchEmpty.size(), "Empty query must return all entries.");

        List<GlossaryEntry> searchNull = glossaryService.search(null);
        assertEquals(all.size(), searchNull.size(), "Null query must return all entries.");
    }

    @Test
    public void testSearchNonExistentTerm() {
        List<GlossaryEntry> results = glossaryService.search("NonExistentTerm123456");
        assertTrue(results.isEmpty(), "Non-existent term must return an empty list.");
    }
}
