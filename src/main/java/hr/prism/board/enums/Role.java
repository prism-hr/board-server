package hr.prism.board.enums;

import com.google.common.collect.ImmutableList;

import java.util.List;

public enum Role {

    ADMINISTRATOR,
    AUTHOR,
    MEMBER,
    PUBLIC;

    public static final List<Role> NON_MEMBER_ROLES = ImmutableList.of(ADMINISTRATOR, AUTHOR);

}
