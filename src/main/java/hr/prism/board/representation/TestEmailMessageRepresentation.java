package hr.prism.board.representation;

import java.util.ArrayList;
import java.util.List;

public class TestEmailMessageRepresentation {

    private UserRepresentation recipient;

    private String subject;

    private String content;

    private List<String> attachments = new ArrayList<>();

    public UserRepresentation getRecipient() {
        return recipient;
    }

    public TestEmailMessageRepresentation setRecipient(UserRepresentation recipient) {
        this.recipient = recipient;
        return this;
    }

    public String getSubject() {
        return subject;
    }

    public TestEmailMessageRepresentation setSubject(String subject) {
        this.subject = subject;
        return this;
    }

    public String getContent() {
        return content;
    }

    public TestEmailMessageRepresentation setContent(String content) {
        this.content = content;
        return this;
    }

    public List<String> getAttachments() {
        return attachments;
    }

    public TestEmailMessageRepresentation setAttachments(List<String> attachments) {
        this.attachments = attachments;
        return this;
    }

}
