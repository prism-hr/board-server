package hr.prism.board.util;

import com.google.common.base.Joiner;
import hr.prism.board.dto.ResourcePatchDTO;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.ExceptionCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.Range;
import org.apache.commons.text.RandomStringGenerator;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class BoardUtils {

    public static DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/YYYY");

    public static RandomStringGenerator RANDOM_STRING_GENERATOR =
        new RandomStringGenerator.Builder()
            .withinRange('0', 'z')
            .filteredBy((codePoint) -> Range.between(48, 57).contains(codePoint) || Range.between(97, 122).contains(codePoint))
            .build();

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
        String[] emailParts = email.split("@");

        List<String> newNameParts = new ArrayList<>();
        for (String namePart : emailParts[0].split("\\.")) {
            int subPartLength = namePart.length();
            for (int i = 0; i < subPartLength; i++) {
                if (i > 0 && !(i > 2 && i == (subPartLength - 1))) {
                    StringBuilder addressPartBuilder = new StringBuilder(namePart);
                    addressPartBuilder.setCharAt(i, '.');
                    namePart = addressPartBuilder.toString();
                }
            }
            newNameParts.add(namePart);
        }

        return Joiner.on(".").join(newNameParts) + "@" + emailParts[1];
    }

    public static String randomAlphanumericString(int length) {
        return RANDOM_STRING_GENERATOR.generate(length);
    }

    public static String getClientIpAddress(HttpServletRequest request) {
        String chain = request.getHeader("X-Forwarded-For");
        if (chain == null) {
            return request.getRemoteAddr();
        }

        return chain.split(", ")[0];
    }

}
