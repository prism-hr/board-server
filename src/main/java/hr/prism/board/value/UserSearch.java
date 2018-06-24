package hr.prism.board.value;

public class UserSearch extends DocumentSearch {

    private Long id;

    private String givenName;

    private String surname;

    private String emailDisplay;

    public UserSearch(Long id, String givenName, String surname, String emailDisplay, String documentCloudinaryId,
                      String documentCloudinaryUrl, String documentFileName) {
        super(documentCloudinaryId, documentCloudinaryUrl, documentFileName);
        this.id = id;
        this.givenName = givenName;
        this.surname = surname;
        this.emailDisplay = emailDisplay;
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

}
