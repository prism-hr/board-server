package hr.prism.board.enums;

public enum ResourceEvent {

    VIEW(false),
    CLICK(true),
    DOWNLOAD(true),
    EMAIL(true);

    private boolean response;

    ResourceEvent(boolean response) {
        this.response = response;
    }

    public boolean isResponse() {
        return response;
    }

}
