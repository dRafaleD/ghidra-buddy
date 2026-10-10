package ghidracompanion;

import ghidracompanion.bridge.JsonHelper;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class JsonHelperTest {

    @Test
    public void testQuoteEscaping() {
        assertEquals("\"hello\"", JsonHelper.quote("hello"));
        assertEquals("\"hello \\\"world\\\"\"", JsonHelper.quote("hello \"world\""));
        assertEquals("\"line1\\nline2\"", JsonHelper.quote("line1\nline2"));
        assertEquals("\"\"", JsonHelper.quote(""));
        assertEquals("\"\"", JsonHelper.quote(null));
    }

    @Test
    public void testMapToJson() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", "main");
        map.put("count", 42);
        map.put("active", true);

        String json = JsonHelper.mapToJson(map);
        assertEquals("{\"name\":\"main\",\"count\":42,\"active\":true}", json);
    }

    @Test
    public void testListToJson() {
        List<String> list = Arrays.asList("a", "b", "c");
        String json = JsonHelper.listToJson(list);
        assertEquals("[\"a\",\"b\",\"c\"]", json);
    }
}
