package hr.prism.board.util;

import java.util.LinkedHashMap;

public class ObjectUtils {
    
    public static LinkedHashMap<String, String> orderedMap(String... keysAndValues) throws IllegalStateException {
        if (keysAndValues == null || keysAndValues.length % 2 != 0) {
            throw new IllegalStateException("Invalid map definition");
        }
        
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < keysAndValues.length; i = (i + 2)) {
            map.put(keysAndValues[i], keysAndValues[i + 1]);
        }
        
        return map;
    }
    
}
