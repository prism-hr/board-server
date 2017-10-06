package hr.prism.board.representation;

import hr.prism.board.enums.AgeRange;
import hr.prism.board.enums.DocumentRequestState;
import hr.prism.board.enums.Gender;
import hr.prism.board.enums.Scope;

import java.util.List;
import java.util.Objects;

public class UserRepresentation {

    private Long id;

    private String givenName;

    private String surname;

    private String email;

    private DocumentRepresentation documentImage;

    private DocumentRequestState documentImageRequestState;

    private Gender gender;

    private AgeRange ageRange;

    private LocationRepresentation locationNationality;

    private DocumentRepresentation documentResume;

    private String websiteResume;

    private List<Scope> scopes;

    private String defaultOrganizationName;

    private LocationRepresentation defaultLocation;

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

    public Gender getGender() {
        return gender;
    }

    public UserRepresentation setGender(Gender gender) {
        this.gender = gender;
        return this;
    }

    public AgeRange getAgeRange() {
        return ageRange;
    }

    public UserRepresentation setAgeRange(AgeRange ageRange) {
        this.ageRange = ageRange;
        return this;
    }

    public LocationRepresentation getLocationNationality() {
        return locationNationality;
    }

    public UserRepresentation setLocationNationality(LocationRepresentation locationNationality) {
        this.locationNationality = locationNationality;
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

    public String getDefaultOrganizationName() {
        return defaultOrganizationName;
    }

    public UserRepresentation setDefaultOrganizationName(String defaultOrganizationName) {
        this.defaultOrganizationName = defaultOrganizationName;
        return this;
    }

    public LocationRepresentation getDefaultLocation() {
        return defaultLocation;
    }

    public UserRepresentation setDefaultLocation(LocationRepresentation defaultLocation) {
        this.defaultLocation = defaultLocation;
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || object.getClass() != getClass()) {
            return false;
        }

        return Objects.equals(email, ((UserRepresentation) object).getEmail());
    }

}
