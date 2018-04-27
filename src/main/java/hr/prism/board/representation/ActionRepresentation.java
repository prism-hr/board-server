package hr.prism.board.representation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.Scope;
import hr.prism.board.enums.State;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import static org.apache.commons.lang3.ObjectUtils.compare;

@JsonIgnoreProperties({"suppressedInOwnerState", "activity", "notification"})
public class ActionRepresentation implements Comparable<ActionRepresentation> {

    private Action action;

    private Scope scope;

    private State state;

    private State suppressedInOwnerState;

    private String activity;

    private String notification;

    public Action getAction() {
        return action;
    }

    public ActionRepresentation setAction(Action action) {
        this.action = action;
        return this;
    }

    public Scope getScope() {
        return scope;
    }

    public ActionRepresentation setScope(Scope scope) {
        this.scope = scope;
        return this;
    }

    public State getState() {
        return state;
    }

    public ActionRepresentation setState(State state) {
        this.state = state;
        return this;
    }

    public State getSuppressedInOwnerState() {
        return suppressedInOwnerState;
    }

    public ActionRepresentation setSuppressedInOwnerState(State suppressedInOwnerState) {
        this.suppressedInOwnerState = suppressedInOwnerState;
        return this;
    }

    public String getActivity() {
        return activity;
    }

    public ActionRepresentation setActivity(String activity) {
        this.activity = activity;
        return this;
    }

    public String getNotification() {
        return notification;
    }

    public ActionRepresentation setNotification(String notification) {
        this.notification = notification;
        return this;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(action)
            .toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        ActionRepresentation that = (ActionRepresentation) other;
        return new EqualsBuilder()
            .append(action, that.action)
            .isEquals();
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public int compareTo(ActionRepresentation other) {
        return compare(action, other.getAction());
    }

}
