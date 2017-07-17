package hr.prism.board.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum State {

    DRAFT, SUSPENDED, PENDING, ACCEPTED, EXPIRED, REJECTED, WITHDRAWN, PREVIOUS;

    public static final List<State> ACTIVE_USER_ROLE_STATES = Arrays.asList(PENDING, ACCEPTED);

    public static final List<String> ACTIVE_USER_ROLE_STATE_STRINGS = ACTIVE_USER_ROLE_STATES.stream().map(State::name).collect(Collectors.toList());

}
