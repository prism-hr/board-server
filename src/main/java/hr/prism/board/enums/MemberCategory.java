package hr.prism.board.enums;

import org.apache.commons.collections.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public enum MemberCategory {

    UNDERGRADUATE_STUDENT,
    MASTER_STUDENT,
    RESEARCH_STUDENT,
    RESEARCH_STAFF,
    ACADEMIC_STAFF,
    PROFESSIONAL_STAFF;

    public static List<String> toStrings(List<MemberCategory> categories) {
        if (CollectionUtils.isEmpty(categories)) {
            return Collections.emptyList();
        }

        return categories.stream().map(MemberCategory::name).collect(Collectors.toList());
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static Optional<List<String>> toStrings(Optional<List<MemberCategory>> categories) {
        if (categories == null) {
            return null;
        } else if (categories.isPresent()) {
            return Optional.of(toStrings(categories.get()));
        }

        return Optional.empty();
    }

    public static List<MemberCategory> fromStrings(List<String> categories) {
        if (CollectionUtils.isEmpty(categories)) {
            return Collections.emptyList();
        }

        return categories.stream().map(MemberCategory::valueOf).collect(Collectors.toList());
    }

}
