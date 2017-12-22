package hr.prism.board.enums;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.stream.Collectors;

public enum State {

    DRAFT, // Board filter, Post filter
    SUSPENDED, // Post filter
    PENDING, // Post filter
    TRIAL, // Department filter
    CONVERSION, // Department filter
    ACCEPTED, // Department filter, Board filter, Post filter
    DEFAULTED, // Department filter
    EXPIRED, // Post filter
    REJECTED, // Department filter, Board filter, Post filter
    WITHDRAWN, // Post filter
    ARCHIVED, // Post only - not in filter for active posts, provides hard-coded filter for separate archive section
    PREVIOUS;

    public static final List<State> ACTIVE_USER_ROLE_STATES = ImmutableList.of(PENDING, ACCEPTED);

    public static final List<String> ACTIVE_USER_ROLE_STATE_STRINGS = ImmutableList.copyOf(ACTIVE_USER_ROLE_STATES.stream().map(State::name).collect(Collectors.toList()));

    public static final List<State> RESOURCE_STATES_TO_ARCHIVE_FROM = ImmutableList.of(DRAFT, SUSPENDED, EXPIRED, REJECTED, WITHDRAWN);

}
