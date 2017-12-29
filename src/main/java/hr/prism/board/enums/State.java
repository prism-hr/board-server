package hr.prism.board.enums;

import com.google.common.collect.ImmutableList;
import hr.prism.board.enums.Labels.Label;

import java.util.List;
import java.util.stream.Collectors;

public enum State {

    @Label(scope = Scope.DEPARTMENT, value = "Trial")
    @Label(scope = Scope.BOARD, value = "Awaiting moderation")
    @Label(scope = Scope.POST, value = "Awaiting moderation")
    DRAFT,

    @Label(scope = Scope.DEPARTMENT, value = "Awaiting payment")
    @Label(scope = Scope.POST, value = "Awaiting resubmission")
    SUSPENDED,

    @Label(scope = Scope.DEPARTMENT, value = "Awaiting subscription")
    @Label(scope = Scope.POST, value = "Awaiting publication")
    PENDING,

    @Label(scope = Scope.DEPARTMENT, value = "Active")
    @Label(scope = Scope.BOARD, value = "Active")
    @Label(scope = Scope.POST, value = "Active")
    ACCEPTED, // Department filter, Board filter, Post filter
    EXPIRED, // Post filter
    REJECTED, // Department filter, Board filter, Post filter
    WITHDRAWN, // Post filter
    ARCHIVED, // Post only - not in filter for active posts, provides hard-coded filter for separate archive section
    PREVIOUS;

    public static final List<State> ACTIVE_USER_ROLE_STATES = ImmutableList.of(PENDING, ACCEPTED);

    public static final List<String> ACTIVE_USER_ROLE_STATE_STRINGS = ImmutableList.copyOf(ACTIVE_USER_ROLE_STATES.stream().map(State::name).collect(Collectors.toList()));

    public static final List<State> RESOURCE_STATES_TO_ARCHIVE_FROM = ImmutableList.of(DRAFT, SUSPENDED, EXPIRED, REJECTED, WITHDRAWN);

}
