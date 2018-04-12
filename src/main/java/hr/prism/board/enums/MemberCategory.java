package hr.prism.board.enums;

import java.util.List;
import java.util.Optional;

import static hr.prism.board.utils.BoardUtils.isPresent;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

public enum MemberCategory {

    UNDERGRADUATE_STUDENT,
    MASTER_STUDENT,
    RESEARCH_STUDENT,
    RESEARCH_STAFF;

    public static List<String> toStrings(List<MemberCategory> categories) {
        if (isEmpty(categories)) {
            return emptyList();
        }

        return categories.stream().map(MemberCategory::name).collect(toList());
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static Optional<List<String>> toStrings(Optional<List<MemberCategory>> categories) {
        if (isPresent(categories)) {
            return Optional.of(toStrings(categories.get()));
        }

        return Optional.empty();
    }

    public static List<MemberCategory> fromStrings(List<String> categories) {
        if (isEmpty(categories)) {
            return emptyList();
        }

        return categories.stream().map(MemberCategory::valueOf).collect(toList());
    }

}
