package hr.prism.board.enums;

import com.google.common.collect.ImmutableList;

import java.util.List;

public enum Role {

    ADMINISTRATOR,
    AUTHOR,
    MEMBER,
    PUBLIC;

    public static final List<Role> NOTIFIABLE = ImmutableList.of(ADMINISTRATOR, AUTHOR);

}
