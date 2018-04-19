package hr.prism.board.value;

public class UserSearch {

    private Long id;

    private String givenName;

    private String surname;

    private String emailDisplay;

    private String documentImageCloudinaryId;

    private String documentImageCloudinaryUrl;

    private String documentImageFileName;

    public UserSearch(Long id, String givenName, String surname, String emailDisplay, String documentImageCloudinaryId,
                      String documentImageCloudinaryUrl, String documentImageFileName) {
        this.id = id;
        this.givenName = givenName;
        this.surname = surname;
        this.emailDisplay = emailDisplay;
        this.documentImageCloudinaryId = documentImageCloudinaryId;
        this.documentImageCloudinaryUrl = documentImageCloudinaryUrl;
        this.documentImageFileName = documentImageFileName;
    }

    public Long getId() {
        return id;
    }

    public String getGivenName() {
        return givenName;
    }

    public String getSurname() {
        return surname;
    }

    public String getEmailDisplay() {
        return emailDisplay;
    }

    public String getDocumentImageCloudinaryId() {
        return documentImageCloudinaryId;
    }

    public String getDocumentImageCloudinaryUrl() {
        return documentImageCloudinaryUrl;
    }

    public String getDocumentImageFileName() {
        return documentImageFileName;
    }

}
