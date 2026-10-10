package ghidracompanion;

import ghidracompanion.model.CodeContext;
import ghidracompanion.model.CodeContext.ActionSuggestion;
import ghidracompanion.model.CodeContext.ContextType;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class ContextModelTest {

    @Test
    public void testEmptyContextFactory() {
        CodeContext empty = CodeContext.empty(null);
        assertNotNull(empty);
        assertNull(empty.getAddress());
        assertEquals(ContextType.GENERAL, empty.getContextType());
        assertFalse(empty.getWhatItIs().isEmpty());
        assertTrue(empty.getSuggestions().isEmpty());
    }

    @Test
    public void testCustomContextCreation() {
        ActionSuggestion suggestion = new ActionSuggestion("Navigate to Target", "GOTO", null);
        CodeContext ctx = new CodeContext(
            null,
            "test_function",
            ContextType.FUNCTION_DEF,
            "Test description",
            "Why it matters",
            "Next step",
            Collections.singletonList(suggestion)
        );

        assertEquals("test_function", ctx.getItemName());
        assertEquals(ContextType.FUNCTION_DEF, ctx.getContextType());
        assertEquals("Test description", ctx.getWhatItIs());
        assertEquals(1, ctx.getSuggestions().size());
        assertEquals("Navigate to Target", ctx.getSuggestions().get(0).getLabel());
    }
}
