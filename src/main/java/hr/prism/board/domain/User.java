package hr.prism.board.domain;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import hr.prism.board.enums.*;
import org.hibernate.validator.constraints.Email;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static hr.prism.board.utils.BoardUtils.obfuscateEmail;
import static javax.persistence.EnumType.STRING;
import static org.apache.commons.lang3.ObjectUtils.compare;

@Entity
@Table(name = "user")
@NamedEntityGraph(
    name = "user.extended",
    attributeNodes = {
        @NamedAttributeNode(value = "userRoles", subgraph = "userRole"),
        @NamedAttributeNode(value = "defaultOrganization"),
        @NamedAttributeNode(value = "defaultLocation")},
    subgraphs = {
        @NamedSubgraph(
            name = "userRole",
            attributeNodes = {
                @NamedAttributeNode(value = "resource")})})
@NamedNativeQuery(
    name = "userSearch",
    query =
        "SELECT user.id AS id, user.given_name  AS givenName, user.surname AS surname, " +
            "user.email_display AS emailDisplay, document_image.cloudinary_id AS documentCloudinaryId, " +
            "document_image.cloudinary_url AS documentCloudinaryUrl, document_image.file_name AS documentFileName " +
            "FROM user " +
            "LEFT JOIN document AS document_image " +
            "ON user.document_image_id = document_image.id " +
            "WHERE user.given_name LIKE :searchTerm " +
            "OR user.surname LIKE :searchTerm " +
            "OR CONCAT(user.given_name, ' ', user.surname) LIKE :searchTerm " +
            "OR user.email LIKE :searchTerm " +
            "ORDER BY user.given_name, user.surname, user.email " +
            "LIMIT 10",
    resultSetMapping = "userSearch")
@SqlResultSetMapping(
    name = "userSearch",
    classes = @ConstructorResult(
        targetClass = hr.prism.board.value.UserSearch.class,
        columns = {
            @ColumnResult(name = "id", type = Long.class),
            @ColumnResult(name = "givenName", type = String.class),
            @ColumnResult(name = "surname", type = String.class),
            @ColumnResult(name = "emailDisplay", type = String.class),
            @ColumnResult(name = "documentCloudinaryId", type = String.class),
            @ColumnResult(name = "documentCloudinaryUrl", type = String.class),
            @ColumnResult(name = "documentFileName", type = String.class)}))
@SuppressWarnings("SqlResolve")
public class User extends BoardEntity implements Comparable<User> {

    @Column(name = "uuid", nullable = false)
    private String uuid;

    @Column(name = "given_name", nullable = false)
    private String givenName;

    @Column(name = "surname", nullable = false)
    private String surname;

    @Email
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "email_display", nullable = false)
    private String emailDisplay;

    @Column(name = "password")
    private String password;

    @Enumerated(STRING)
    @Column(name = "password_hash")
    private PasswordHash passwordHash;

    @Column(name = "password_reset_uuid")
    private String passwordResetUuid;

    @Column(name = "password_reset_timestamp")
    private LocalDateTime passwordResetTimestamp;

    @Enumerated(STRING)
    @Column(name = "oauth_provider")
    private OauthProvider oauthProvider;

    @Column(name = "oauth_account_id")
    private String oauthAccountId;

    @Column(name = "test_user")
    private Boolean testUser;

    @OneToOne
    @JoinColumn(name = "document_image_id")
    private Document documentImage;

    @Enumerated(STRING)
    @Column(name = "document_image_request_state")
    private DocumentRequestState documentImageRequestState;

    @Column(name = "seen_walk_through")
    private Boolean seenWalkThrough;

    @Enumerated(STRING)
    @Column(name = "gender")
    private Gender gender;

    @Enumerated(STRING)
    @Column(name = "age_range")
    private AgeRange ageRange;

    @ManyToOne
    @JoinColumn(name = "location_nationality_id")
    private Location locationNationality;

    @OneToOne
    @JoinColumn(name = "document_resume_id")
    private Document documentResume;

    @Column(name = "website_resume")
    private String websiteResume;

    @ManyToOne
    @JoinColumn(name = "default_organization_id")
    private Organization defaultOrganization;

    @ManyToOne
    @JoinColumn(name = "default_location_id")
    private Location defaultLocation;

    @Column(name = "index_data")
    private String indexData;

    @OneToMany(mappedBy = "user")
    private Set<UserRole> userRoles = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<UserSearch> searches = new HashSet<>();

    @Transient
    private boolean departmentAdministrator;

    @Transient
    private boolean postCreator;

    @Transient
    private boolean revealEmail;

    public String getUuid() {
        return uuid;
    }

    public User setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public String getGivenName() {
        return givenName;
    }

    public User setGivenName(String givenName) {
        this.givenName = givenName;
        return this;
    }

    public String getSurname() {
        return surname;
    }

    public User setSurname(String surname) {
        this.surname = surname;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public User setEmail(String email) {
        this.email = email;
        this.emailDisplay = obfuscateEmail(email);
        return this;
    }

    public String getEmailDisplay() {
        return emailDisplay;
    }

    public String getPassword() {
        return password;
    }

    public User setPassword(String password) {
        this.password = password;
        return this;
    }

    @SuppressWarnings("unused")
    public PasswordHash getPasswordHash() {
        return passwordHash;
    }

    public User setPasswordHash(PasswordHash passwordHash) {
        this.passwordHash = passwordHash;
        return this;
    }

    public String getPasswordResetUuid() {
        return passwordResetUuid;
    }

    @SuppressWarnings("UnusedReturnValue")
    public User setPasswordResetUuid(String passwordResetUuid) {
        this.passwordResetUuid = passwordResetUuid;
        return this;
    }

    public LocalDateTime getPasswordResetTimestamp() {
        return passwordResetTimestamp;
    }

    @SuppressWarnings("UnusedReturnValue")
    public User setPasswordResetTimestamp(LocalDateTime passwordResetTimestamp) {
        this.passwordResetTimestamp = passwordResetTimestamp;
        return this;
    }

    public OauthProvider getOauthProvider() {
        return oauthProvider;
    }

    public User setOauthProvider(OauthProvider oauthProvider) {
        this.oauthProvider = oauthProvider;
        return this;
    }

    public String getOauthAccountId() {
        return oauthAccountId;
    }

    public User setOauthAccountId(String oauthAccountId) {
        this.oauthAccountId = oauthAccountId;
        return this;
    }

    @SuppressWarnings("unused")
    public Boolean getTestUser() {
        return testUser;
    }

    @SuppressWarnings("UnusedReturnValue")
    public User setTestUser(Boolean testUser) {
        this.testUser = testUser;
        return this;
    }

    public Document getDocumentImage() {
        return documentImage;
    }

    public User setDocumentImage(Document documentImage) {
        this.documentImage = documentImage;
        return this;
    }

    public DocumentRequestState getDocumentImageRequestState() {
        return documentImageRequestState;
    }

    public User setDocumentImageRequestState(DocumentRequestState documentImageRequestState) {
        this.documentImageRequestState = documentImageRequestState;
        return this;
    }

    public Boolean getSeenWalkThrough() {
        return seenWalkThrough;
    }

    public User setSeenWalkThrough(Boolean seenWalkThrough) {
        this.seenWalkThrough = seenWalkThrough;
        return this;
    }

    public Gender getGender() {
        return gender;
    }

    public User setGender(Gender gender) {
        this.gender = gender;
        return this;
    }

    public AgeRange getAgeRange() {
        return ageRange;
    }

    public User setAgeRange(AgeRange ageRange) {
        this.ageRange = ageRange;
        return this;
    }

    public Location getLocationNationality() {
        return locationNationality;
    }

    public User setLocationNationality(Location locationNationality) {
        this.locationNationality = locationNationality;
        return this;
    }

    public Document getDocumentResume() {
        return documentResume;
    }

    public User setDocumentResume(Document documentResume) {
        this.documentResume = documentResume;
        return this;
    }

    public String getWebsiteResume() {
        return websiteResume;
    }

    public User setWebsiteResume(String websiteResume) {
        this.websiteResume = websiteResume;
        return this;
    }

    public Organization getDefaultOrganization() {
        return defaultOrganization;
    }

    @SuppressWarnings("UnusedReturnValue")
    public User setDefaultOrganization(Organization defaultOrganization) {
        this.defaultOrganization = defaultOrganization;
        return this;
    }

    public Location getDefaultLocation() {
        return defaultLocation;
    }

    @SuppressWarnings("UnusedReturnValue")
    public User setDefaultLocation(Location defaultLocation) {
        this.defaultLocation = defaultLocation;
        return this;
    }

    @SuppressWarnings("unused")
    public String getIndexData() {
        return indexData;
    }

    @SuppressWarnings("UnusedReturnValue")
    public User setIndexData(String indexData) {
        this.indexData = indexData;
        return this;
    }

    public Set<UserRole> getUserRoles() {
        return userRoles;
    }

    @SuppressWarnings("unused")
    public Set<UserSearch> getSearches() {
        return searches;
    }

    public boolean isDepartmentAdministrator() {
        return departmentAdministrator;
    }

    @SuppressWarnings("UnusedReturnValue")
    public User setDepartmentAdministrator(boolean departmentAdministrator) {
        this.departmentAdministrator = departmentAdministrator;
        return this;
    }

    public boolean isPostCreator() {
        return postCreator;
    }

    @SuppressWarnings("UnusedReturnValue")
    public User setPostCreator(boolean postCreator) {
        this.postCreator = postCreator;
        return this;
    }

    public boolean isRevealEmail() {
        return revealEmail;
    }

    @SuppressWarnings("UnusedReturnValue")
    public User setRevealEmail(boolean revealEmail) {
        this.revealEmail = revealEmail;
        return this;
    }

    public String getFullName() {
        return Joiner.on(" ").skipNulls().join(givenName, surname);
    }

    public boolean isRegistered() {
        return password != null || oauthProvider != null;
    }

    public boolean passwordMatches(String input) {
        return passwordHash.matches(input, password);
    }

    public List<String> getIndexDataParts() {
        return ImmutableList.of(givenName, surname);
    }

    @Override
    public String toString() {
        return Joiner.on(" ").skipNulls().join(givenName, surname, email);
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public int compareTo(User other) {
        int compare = compare(givenName, other.getGivenName());
        compare = compare == 0 ? compare(surname, other.getSurname()) : compare;
        return compare == 0 ? compare(getId(), other.getId()) : compare;
    }

}
