package ghidracompanion.bridge;

import java.util.Collection;
import java.util.Map;

/**
 * Lightweight, zero-dependency JSON serializer for bridge responses.
 */
public class JsonHelper {

    public static String quote(String string) {
        if (string == null || string.isEmpty()) {
            return "\"\"";
        }

        StringBuilder sb = new StringBuilder(string.length() + 4);
        sb.append('"');
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            switch (c) {
                case '\\':
                case '"':
                    sb.append('\\').append(c);
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                default:
                    if (c < ' ') {
                        String t = "000" + Integer.toHexString(c);
                        sb.append("\\u").append(t.substring(t.length() - 4));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
        return sb.toString();
    }

    public static String mapToJson(Map<String, ?> map) {
        if (map == null) return "{}";
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            if (!first) sb.append(",");
            sb.append(quote(entry.getKey())).append(":");
            sb.append(valueToJson(entry.getValue()));
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    public static String listToJson(Collection<?> list) {
        if (list == null) return "[]";
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Object item : list) {
            if (!first) sb.append(",");
            sb.append(valueToJson(item));
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }

    public static String valueToJson(Object val) {
        if (val == null) {
            return "null";
        }
        if (val instanceof String) {
            return quote((String) val);
        }
        if (val instanceof Number || val instanceof Boolean) {
            return val.toString();
        }
        if (val instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, ?> map = (Map<String, ?>) val;
            return mapToJson(map);
        }
        if (val instanceof Collection) {
            return listToJson((Collection<?>) val);
        }
        return quote(val.toString());
    }
}
