package hr.prism.board.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum State {

    DRAFT, // Board filter, Post filter
    SUSPENDED, // Post filter
    PENDING, // Post filter
    ACCEPTED, // Board filter, Post filter
    EXPIRED, // Post filter
    REJECTED, // Board filter, Post filter
    WITHDRAWN, // Post filter
    ARCHIVED, // Post only - not in filter for active posts, provides hard-coded filter for separate archive section
    PREVIOUS;

    public static final List<State> ACTIVE_USER_ROLE_STATES = Arrays.asList(PENDING, ACCEPTED);

    public static final List<String> ACTIVE_USER_ROLE_STATE_STRINGS = ACTIVE_USER_ROLE_STATES.stream().map(State::name).collect(Collectors.toList());

    public static final List<State> RESOURCE_STATES_TO_ARCHIVE_FROM = Arrays.asList(DRAFT, SUSPENDED, EXPIRED, REJECTED, WITHDRAWN);

}
