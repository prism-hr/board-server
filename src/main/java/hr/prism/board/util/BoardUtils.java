package hr.prism.board.util;

import hr.prism.board.dto.ResourcePatchDTO;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.ExceptionCode;
import org.apache.commons.collections.CollectionUtils;

import java.lang.reflect.Field;
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

    public static <T extends ResourcePatchDTO> boolean hasUpdates(T resourceDTO) {
        for (Field field : resourceDTO.getClass().getFields()) {
            field.setAccessible(true);
            if (!field.getName().equals("comment")) {
                try {
                    if (field.get(resourceDTO) != null) {
                        return true;
                    }
                } catch (IllegalAccessException e) {
                    throw new BoardException(ExceptionCode.PROBLEM, e);
                }
            }
        }

        return false;
    }

}
