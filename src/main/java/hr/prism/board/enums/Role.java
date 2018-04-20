package hr.prism.board.enums;

import com.google.common.collect.ImmutableList;

import java.util.List;

import static hr.prism.board.enums.RoleType.STAFF;

public enum Role {

    ADMINISTRATOR(STAFF),
    AUTHOR(STAFF),
    MEMBER(RoleType.MEMBER),
    PUBLIC(RoleType.PUBLIC);

    public static final List<Role> STAFF_ROLES = ImmutableList.of(ADMINISTRATOR, AUTHOR);

    public static final List<Role> MEMBER_ROLES = ImmutableList.of(MEMBER);

    private RoleType type;

    Role(RoleType type) {
        this.type = type;
    }

    public RoleType getType() {
        return type;
    }

}
