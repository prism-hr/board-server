package hr.prism.board.utils;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.ResourceCategory;
import hr.prism.board.enums.CategoryType;
import hr.prism.board.enums.Scope;
import hr.prism.board.enums.State;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.value.ResourceFilter;

import java.util.List;

import static hr.prism.board.enums.State.ARCHIVED;
import static hr.prism.board.exception.ExceptionCode.INVALID_RESOURCE_FILTER;
import static java.lang.Integer.parseInt;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.*;

public class ResourceUtils {

    private static final int MAX_HANDLE_LENGTH = 25;

    public static String suggestHandle(String name) {
        String suggestion = "";
        name = stripAccents(name.toLowerCase());
        String[] parts = name.split(" ");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (isAlphanumeric(part)) {
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
            List<String> similarHandleSuffixes = similarHandles.stream()
                .map(similarHandle -> similarHandle.substring(suggestedHandleLength))
                .collect(toList());

            for (String similarHandleSuffix : similarHandleSuffixes) {
                if (similarHandleSuffix.startsWith("-")) {
                    String[] parts = similarHandleSuffix.replaceFirst("-", "").split("-");

                    // We only care about creating a unique value in a formatted sequence
                    // We can ignore anything else that has been reformatted by an end user
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

    public static ResourceFilter makeResourceFilter(Scope scope, Long parentId, Boolean includePublic, State state,
                                                    String quarter, String searchTerm) {
        String stateString = null;
        String negatedStateString = ARCHIVED.name();
        if (state != null) {
            stateString = state.name();
            if (state == ARCHIVED) {
                negatedStateString = null;
                if (quarter == null) {
                    throw new BoardException(INVALID_RESOURCE_FILTER,
                        "Cannot search archive without specifying quarter");
                }
            }
        }

        return new ResourceFilter()
            .setScope(scope)
            .setParentId(parentId)
            .setState(stateString)
            .setNegatedState(negatedStateString)
            .setQuarter(quarter)
            .setSearchTerm(searchTerm)
            .setIncludePublicResources(includePublic);
    }

    public static void validateCategories(Resource reference, CategoryType type, List<String> categories,
                                          ExceptionCode missing, ExceptionCode invalid, ExceptionCode corrupted) {
        List<ResourceCategory> referenceCategories = reference.getCategories(type);
        if (!referenceCategories.isEmpty()) {
            if (isEmpty(categories)) {
                throw new BoardException(missing, "Categories must be specified");
            } else if (
                !referenceCategories
                    .stream()
                    .map(ResourceCategory::getName)
                    .collect(toList())
                    .containsAll(categories)) {
                throw new BoardException(invalid, "Valid categories must be specified - check parent categories");
            }
        } else if (isNotEmpty(categories)) {
            throw new BoardException(corrupted, "Categories must not be specified");
        }
    }

}