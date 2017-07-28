package hr.prism.board.util;

import java.util.LinkedHashMap;

public class ObjectUtils {

    public static LinkedHashMap<String, Object> orderedMap(Object... keysAndValues) throws IllegalStateException {
        if (keysAndValues == null || keysAndValues.length % 2 != 0) {
            throw new IllegalStateException("Invalid map definition");
        }

        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < keysAndValues.length; i = (i + 2)) {
            map.put(keysAndValues[i].toString(), keysAndValues[i + 1]);
        }

        return map;
    }

}
