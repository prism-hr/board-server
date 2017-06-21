package hr.prism.board.util;

import com.google.common.base.Joiner;
import hr.prism.board.dto.ResourcePatchDTO;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.ExceptionCode;
import org.apache.commons.collections.CollectionUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

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
        List<Field> fields = new ArrayList<>();
        Class<?> dtoClass = resourceDTO.getClass();
        while (ResourcePatchDTO.class.isAssignableFrom(dtoClass)) {
            fields.addAll(Arrays.stream(dtoClass.getDeclaredFields()).collect(Collectors.toList()));
            dtoClass = dtoClass.getSuperclass();
        }

        for (Field field : fields) {
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

    public static String obfuscateEmail(String email) {
        char hashChar = "#".charAt(0);
        String[] emailParts = email.split("@");

        List<String> newNameParts = new ArrayList<>();
        for (String namePart : emailParts[0].split("\\.")) {
            int subPartLength = namePart.length();
            for (int i = 0; i < subPartLength; i++) {
                if (i > 0 && !(i > 2 && i == (subPartLength - 1))) {
                    StringBuilder addressPartBuilder = new StringBuilder(namePart);
                    addressPartBuilder.setCharAt(i, hashChar);
                    namePart = addressPartBuilder.toString();
                }
            }
            newNameParts.add(namePart);
        }

        return Joiner.on(".").join(newNameParts) + "@" + emailParts[1];
    }

}
