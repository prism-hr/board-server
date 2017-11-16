package hr.prism.board.notification;

import com.sendgrid.Attachments;

public class BoardAttachments extends Attachments {

    private String url;

    public String getUrl() {
        return url;
    }

    public BoardAttachments setUrl(String url) {
        this.url = url;
        return this;
    }

}
