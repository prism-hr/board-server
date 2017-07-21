package hr.prism.board.workflow;

public class Activity extends Update<Activity> {

    private hr.prism.board.enums.Activity category;

    public hr.prism.board.enums.Activity getCategory() {
        return category;
    }

    public Activity setCategory(hr.prism.board.enums.Activity category) {
        this.category = category;
        return this;
    }

    public Workflow with(hr.prism.board.enums.Activity category) {
        this.category = category;
        return getWorkflow();
    }

}
