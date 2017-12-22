package hr.prism.board.enums;

public enum ResourceTask {

    CREATE_MEMBER("Ready to get going - visit the user section to build your student list."),
    UPDATE_MEMBER("New students arriving - visit the user section to sign them up."),
    CREATE_POST("Got something to share - visit the new post section to create some content."),
    DEPLOY_BADGE("Time to spread the word - visit the badges section to get embed codes for your website.");

    ResourceTask(String message) {
        this.message = message;
    }

    private String message;

    public String getMessage() {
        return message;
    }

}
