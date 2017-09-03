package hr.prism.board.util;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import hr.prism.board.dto.ResourcePatchDTO;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.ExceptionCode;
import opennlp.tools.tokenize.SimpleTokenizer;
import org.apache.commons.codec.language.Soundex;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.Range;
import org.apache.commons.text.RandomStringGenerator;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BoardUtils {

    public static DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/YYYY");

    public static RandomStringGenerator RANDOM_STRING_GENERATOR =
        new RandomStringGenerator.Builder()
            .withinRange('0', 'z')
            .filteredBy((codePoint) -> Range.between(48, 57).contains(codePoint) || Range.between(97, 122).contains(codePoint))
            .build();

    private static SimpleTokenizer TOKENIZER = SimpleTokenizer.INSTANCE;

    private static List<String> STOPWORDS = Collections.unmodifiableList(
        Lists.newArrayList("a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any", "are", "aren't", "as", "at", "be", "because", "been", "before",
            "being", "below", "between", "both", "but", "by", "can't", "cannot", "could", "couldn't", "did", "didn't", "do", "does", "doesn't", "doing", "don't", "down", "during",
            "each", "few", "for", "from", "further", "had", "hadn't", "has", "hasn't", "have", "haven't", "having", "he", "he'd", "he'll", "he's", "her", "here", "here's", "hers",
            "herself", "him", "himself", "his", "how", "how's", "i", "i'd", "i'll", "i'm", "i've", "if", "in", "into", "is", "isn't", "it", "it's", "its", "itself", "let's", "me",
            "more", "most", "mustn't", "my", "myself", "no", "nor", "not", "of", "off", "on", "once", "only", "or", "other", "ought", "our", "ours", "ourselves", "out", "over",
            "own", "same", "shan't", "she", "she'd", "she'll", "she's", "should", "shouldn't", "so", "some", "such", "than", "that", "that's", "the", "their", "theirs", "them",
            "themselves", "then", "there", "there's", "these", "they", "they'd", "they'll", "they're", "they've", "this", "those", "through", "to", "too", "under", "until", "up",
            "very", "was", "wasn't", "we", "we'd", "we'll", "we're", "we've", "were", "weren't", "what", "what's", "when", "when's", "where", "where's", "which", "while", "who",
            "who's", "whom", "why", "why's", "with", "won't", "would", "wouldn't", "you", "you'd", "you'll", "you're", "you've", "your", "yours", "yourself", "yourselves")
            .stream().map(TOKENIZER::tokenize).flatMap(Arrays::stream).collect(Collectors.toList()));

    private static Soundex SOUNDEX = new Soundex();

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

    public static String makeSoundexRemovingStopWords(String... strings) {
        List<String> nullSafeStrings = Stream.of(strings).filter(Objects::nonNull).collect(Collectors.toList());
        if (nullSafeStrings.isEmpty()) {
            return null;
        }

        return nullSafeStrings.stream()
            .map(String::toLowerCase)
            .map(TOKENIZER::tokenize)
            .flatMap(Arrays::stream)
            .filter(string -> string.length() > 1)
            .filter(string -> !STOPWORDS.contains(string))
            .collect(Collectors.joining(" "));
    }

}
