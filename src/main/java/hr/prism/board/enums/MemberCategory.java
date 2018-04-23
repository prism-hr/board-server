package hr.prism.board.enums;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

public enum MemberCategory {

    UNDERGRADUATE_STUDENT,
    MASTER_STUDENT,
    RESEARCH_STUDENT,
    RESEARCH_STAFF;

    public static List<String> MEMBER_CATEGORY_STRINGS =
        Stream.of(values()).map(MemberCategory::name).collect(toList());

    public static List<String> toStrings(List<MemberCategory> categories) {
        if (isEmpty(categories)) {
            return emptyList();
        }

        return categories.stream().map(MemberCategory::name).collect(toList());
    }

    public static List<MemberCategory> fromStrings(List<String> categories) {
        if (isEmpty(categories)) {
            return emptyList();
        }

        return categories.stream().map(MemberCategory::valueOf).collect(toList());
    }

}
