package hr.prism.board.workflow;

public class Activity extends Update<Activity> {

    private hr.prism.board.enums.Activity activity;

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

}
