package hr.prism.board.enums;

import com.google.common.collect.ImmutableList;

import java.util.List;

import static java.util.stream.Collectors.toList;

public enum State {

    DRAFT,
    SUSPENDED,
    PENDING,
    ACCEPTED,
    EXPIRED,
    REJECTED,
    WITHDRAWN,
    ARCHIVED,
    PREVIOUS;

    public static final List<State> ACTIVE_USER_ROLE_STATES = ImmutableList.of(PENDING, ACCEPTED);

    public static final List<String> ACTIVE_USER_ROLE_STATE_STRINGS =
        ACTIVE_USER_ROLE_STATES.stream().map(State::name).collect(toList());

    public static final List<State> RESOURCE_STATES_TO_ARCHIVE_FROM =
        ImmutableList.of(DRAFT, SUSPENDED, EXPIRED, REJECTED, WITHDRAWN);

}
