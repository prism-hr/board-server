package hr.prism.board.representation;

import com.google.common.base.MoreObjects;
import hr.prism.board.enums.DocumentRequestState;
import hr.prism.board.enums.Scope;

import java.util.List;

public class UserRepresentation {

    private Long id;

    private String givenName;

    private String surname;

    private String email;

    private DocumentRepresentation documentImage;

    private DocumentRequestState documentImageRequestState;

    private DocumentRepresentation documentResume;

    private String websiteResume;

    private List<Scope> scopes;

    public Long getId() {
        return id;
    }

    public UserRepresentation setId(Long id) {
        this.id = id;
        return this;
    }

    public String getGivenName() {
        return givenName;
    }

    public UserRepresentation setGivenName(String givenName) {
        this.givenName = givenName;
        return this;
    }

    public String getSurname() {
        return surname;
    }

    public UserRepresentation setSurname(String surname) {
        this.surname = surname;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public UserRepresentation setEmail(String email) {
        this.email = email;
        return this;
    }

    public DocumentRepresentation getDocumentImage() {
        return documentImage;
    }

    public UserRepresentation setDocumentImage(DocumentRepresentation documentImage) {
        this.documentImage = documentImage;
        return this;
    }

    public DocumentRequestState getDocumentImageRequestState() {
        return documentImageRequestState;
    }

    public UserRepresentation setDocumentImageRequestState(DocumentRequestState documentImageRequestState) {
        this.documentImageRequestState = documentImageRequestState;
        return this;
    }

    public DocumentRepresentation getDocumentResume() {
        return documentResume;
    }

    public UserRepresentation setDocumentResume(DocumentRepresentation documentResume) {
        this.documentResume = documentResume;
        return this;
    }

    public String getWebsiteResume() {
        return websiteResume;
    }

    public UserRepresentation setWebsiteResume(String websiteResume) {
        this.websiteResume = websiteResume;
        return this;
    }

    public List<Scope> getScopes() {
        return scopes;
    }

    public UserRepresentation setScopes(List<Scope> scopes) {
        this.scopes = scopes;
        return this;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("name", givenName + " " + surname)
            .add("email", email)
            .toString();
    }

}
