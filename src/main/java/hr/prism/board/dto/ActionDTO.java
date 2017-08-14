package hr.prism.board.dto;

import hr.prism.board.enums.Action;
import hr.prism.board.enums.State;

public class ActionDTO {

    private Action action;

    private State nextState;

    public Action getAction() {
        return action;
    }

    public ActionDTO setAction(Action action) {
        this.action = action;
        return this;
    }

    public State getNextState() {
        return nextState;
    }

    public ActionDTO setNextState(State nextState) {
        this.nextState = nextState;
        return this;
    }
}
