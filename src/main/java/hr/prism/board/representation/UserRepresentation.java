package hr.prism.board.representation;

import hr.prism.board.enums.AgeRange;
import hr.prism.board.enums.DocumentRequestState;
import hr.prism.board.enums.Gender;
import hr.prism.board.enums.Scope;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

@SuppressWarnings("unused")
public class UserRepresentation {

    private Long id;

    private String givenName;

    private String surname;

    private String email;

    private DocumentRepresentation documentImage;

    private DocumentRequestState documentImageRequestState;

    private Boolean seenWalkThrough;

    private Gender gender;

    private AgeRange ageRange;

    private LocationRepresentation locationNationality;

    private DocumentRepresentation documentResume;

    private String websiteResume;

    private boolean departmentAdministrator;

    private boolean postCreator;

    private OrganizationRepresentation defaultOrganization;

    private LocationRepresentation defaultLocation;

    private boolean registered;

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

    public Boolean getSeenWalkThrough() {
        return seenWalkThrough;
    }

    public UserRepresentation setSeenWalkThrough(Boolean seenWalkThrough) {
        this.seenWalkThrough = seenWalkThrough;
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

    public boolean isDepartmentAdministrator() {
        return departmentAdministrator;
    }

    public UserRepresentation setDepartmentAdministrator(boolean departmentAdministrator) {
        this.departmentAdministrator = departmentAdministrator;
        return this;
    }

    public boolean isPostCreator() {
        return postCreator;
    }

    public UserRepresentation setPostCreator(boolean postCreator) {
        this.postCreator = postCreator;
        return this;
    }

    public OrganizationRepresentation getDefaultOrganization() {
        return defaultOrganization;
    }

    public UserRepresentation setDefaultOrganization(OrganizationRepresentation defaultOrganization) {
        this.defaultOrganization = defaultOrganization;
        return this;
    }

    public LocationRepresentation getDefaultLocation() {
        return defaultLocation;
    }

    public UserRepresentation setDefaultLocation(LocationRepresentation defaultLocation) {
        this.defaultLocation = defaultLocation;
        return this;
    }

    public boolean isRegistered() {
        return registered;
    }

    public UserRepresentation setRegistered(boolean registered) {
        this.registered = registered;
        return this;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        UserRepresentation that = (UserRepresentation) other;
        return new EqualsBuilder()
            .append(id, that.id)
            .append(email, that.email)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(id)
            .append(email)
            .toHashCode();
    }
}
