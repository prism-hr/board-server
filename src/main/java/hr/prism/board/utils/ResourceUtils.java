package hr.prism.board.utils;

import java.time.LocalDateTime;
import java.util.List;

import static java.lang.Integer.parseInt;
import static java.lang.Math.ceil;
import static java.util.Comparator.reverseOrder;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNumeric;

public class ResourceUtils {

    private static final int MAX_HANDLE_LENGTH = 25;

    public static String suggestHandle(String name) {
        String simplifiedName = name.toLowerCase().replaceAll("(?!.*[a-z0-9-]).*", EMPTY);
        String[] parts = simplifiedName.split(" ");

        if (parts.length == 1) {
            return simplifiedName.length() > MAX_HANDLE_LENGTH ?
                simplifiedName.substring(0, MAX_HANDLE_LENGTH) :
                simplifiedName;
        }

        String suggestion = EMPTY;
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.matches("^[a-z0-9-]+$")) {
                String newSuggestion;
                if (suggestion.length() > 0) {
                    newSuggestion = suggestion + "-" + part;
                } else {
                    newSuggestion = part;
                }

                if (newSuggestion.length() > MAX_HANDLE_LENGTH) {
                    if (i == 0) {
                        return newSuggestion.substring(0, MAX_HANDLE_LENGTH);
                    }

                    return suggestion;
                }

                suggestion = newSuggestion;
            }
        }

        return suggestion;
    }

    public static String confirmHandle(String suggestedHandle, List<String> similarHandles) {
        if (similarHandles.contains(suggestedHandle)) {
            int ordinal = 2;
            int suggestedHandleLength = suggestedHandle.length();

            List<String> similarHandleSuffixes =
                similarHandles.stream()
                    .map(similarHandle -> similarHandle.substring(suggestedHandleLength))
                    .sorted(reverseOrder())
                    .collect(toList());

            for (String similarHandleSuffix : similarHandleSuffixes) {
                if (similarHandleSuffix.startsWith("-")) {
                    String[] parts = similarHandleSuffix.replaceFirst("-", "").split("-");

                    if (parts.length == 1) {
                        String firstPart = parts[0];
                        if (isNumeric(firstPart)) {
                            ordinal = parseInt(firstPart) + 1;
                            break;
                        }
                    }
                }
            }

            return suggestedHandle + "-" + ordinal;
        }

        return suggestedHandle;
    }

    public static String makeQuarter(LocalDateTime createdTimestamp) {
        return Integer.toString(createdTimestamp.getYear()) + (int) ceil((double) createdTimestamp.getMonthValue() / 3);
    }

}
