package hr.prism.board.util;

import org.apache.commons.collections.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BoardUtils {
    
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static boolean isPresent(Optional<?> optional) {
        return optional != null && optional.isPresent();
    }
    
    public static <T> Map<T, Integer> getOrderIndex(List<T> objects) {
        if (CollectionUtils.isNotEmpty(objects)) {
            Map<T, Integer> index = new HashMap<>();
            for (int i = 0; i < objects.size(); i++) {
                index.put(objects.get(i), i);
            }
            
            return index;
        }
        
        return null;
    }
    
}
