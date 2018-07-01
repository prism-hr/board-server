package hr.prism.board.workflow;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Activity extends Update<Activity> {

    private hr.prism.board.enums.Activity activity;

    public Activity() {
        setType(ACTIVITY);
    }

    public hr.prism.board.enums.Activity getActivity() {
        return activity;
    }

    public Activity setActivity(hr.prism.board.enums.Activity activity) {
        this.activity = activity;
        return this;
    }

    public Workflow with(hr.prism.board.enums.Activity category) {
        this.activity = category;
        return getWorkflow();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .appendSuper(super.hashCode())
            .append(activity)
            .toHashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        Activity activity1 = (Activity) object;
        return new EqualsBuilder()
            .appendSuper(super.equals(object))
            .append(activity, activity1.activity)
            .isEquals();
    }

}
