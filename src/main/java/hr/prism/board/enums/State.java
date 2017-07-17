package hr.prism.board.enums;

import java.util.stream.Stream;

public enum State {

    DRAFT, SUSPENDED, PENDING, ACCEPTED, EXPIRED, REJECTED, WITHDRAWN, PREVIOUS;

    public static final State[] ACTIVE_USER_ROLE_STATES = new State[]{PENDING, ACCEPTED};

    public static final String[] ACTIVE_USER_ROLE_STATE_STRINGS = Stream.of(ACTIVE_USER_ROLE_STATES).map(State::name).toArray(String[]::new);

}
