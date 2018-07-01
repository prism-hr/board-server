package hr.prism.board.utils;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import hr.prism.board.dto.ResourcePatchDTO;
import opennlp.tools.tokenize.SimpleTokenizer;
import org.apache.commons.codec.language.Soundex;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.RandomStringGenerator;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

public class BoardUtils {

    private static RandomStringGenerator RANDOM_STRING_GENERATOR =
        new RandomStringGenerator.Builder()
            .withinRange('0', 'z')
            .filteredBy((codePoint) ->
                Range.between(48, 57).contains(codePoint)
                    || Range.between(97, 122).contains(codePoint))
            .build();

    private static SimpleTokenizer TOKENIZER = SimpleTokenizer.INSTANCE;

    private static Soundex SOUNDEX = new Soundex();

    public static DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/YYYY");

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static boolean isPresent(Optional<?> optional) {
        return optional != null && optional.isPresent();
    }

    public static <T extends ResourcePatchDTO> boolean hasUpdates(T resourceDTO) {
        List<Field> fields = new ArrayList<>();
        Class<?> dtoClass = resourceDTO.getClass();
        while (ResourcePatchDTO.class.isAssignableFrom(dtoClass)) {
            fields.addAll(Arrays.stream(dtoClass.getDeclaredFields()).collect(toList()));
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
                    throw new Error(e);
                }
            }
        }

        return false;
    }

    public static String obfuscateEmail(String email) {
        if (email != null) {
            String[] emailParts = email.split("@");

            List<String> newNameParts = new ArrayList<>();
            for (String namePart : emailParts[0].split("\\.")) {
                int subPartLength = namePart.length();
                for (int i = 0; i < subPartLength; i++) {
                    if (i > 0 && !(i > 1 && i == (subPartLength - 1))) {
                        StringBuilder addressPartBuilder = new StringBuilder(namePart);
                        addressPartBuilder.setCharAt(i, '.');
                        namePart = addressPartBuilder.toString();
                    }
                }
                newNameParts.add(namePart);
            }

            return Joiner.on(".").join(newNameParts) + "@" + emailParts[1];
        }

        return null;
    }

    @SuppressWarnings("SameParameterValue")
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

    public static String makeSoundex(String string) {
        return makeSoundex(singletonList(string));
    }

    public static String makeSoundex(List<String> strings) {
        List<String> nullSafeStrings = strings.stream().filter(Objects::nonNull).collect(toList());
        if (nullSafeStrings.isEmpty()) {
            return null;
        }

        return nullSafeStrings.stream()
            .map(String::toLowerCase)
            .map(TOKENIZER::tokenize)
            .flatMap(Arrays::stream)
            .filter(StringUtils::isNotEmpty)
            .map(StringUtils::stripAccents)
            .map(str -> {
                try {
                    return SOUNDEX.encode(str);
                } catch (IllegalArgumentException e) {
                    throw new Error("Could not encode string: " + str, e);
                }
            })
            .filter(StringUtils::isNotEmpty)
            .collect(joining(" "));
    }

    public static String emptyToNull(String string) {
        return string == null ? null : Strings.emptyToNull(string.trim());
    }

    public static <T> T firstOrNull(List<T> objects) {
        return isEmpty(objects) ? null : objects.get(0);
    }

    public static LocalDate getAcademicYearStart() {
        LocalDate baseline = LocalDate.now();
        if (baseline.getMonthValue() > 9) {
            // Academic year started this year
            return LocalDate.of(baseline.getYear(), 10, 1);
        }

        // Academic year started last year
        return LocalDate.of(baseline.getYear() - 1, 10, 1);
    }

}
