package hr.prism.board.domain;

import com.google.common.base.Joiner;

import org.apache.commons.lang3.ObjectUtils;
import org.hibernate.validator.constraints.Email;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import hr.prism.board.enums.AgeRange;
import hr.prism.board.enums.DocumentRequestState;
import hr.prism.board.enums.Gender;
import hr.prism.board.enums.OauthProvider;
import hr.prism.board.enums.PasswordHash;
import hr.prism.board.enums.Scope;
import hr.prism.board.utils.BoardUtils;

@Entity
@Table(name = "user")
@SuppressWarnings({"unused", "SameParameterValue", "UnusedReturnValue"})
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

    @Enumerated(EnumType.STRING)
    @Column(name = "password_hash")
    private PasswordHash passwordHash;

    @Column(name = "password_reset_uuid")
    private String passwordResetUuid;

    @Column(name = "password_reset_timestamp")
    private LocalDateTime passwordResetTimestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "oauth_provider")
    private OauthProvider oauthProvider;

    @Column(name = "oauth_account_id")
    private String oauthAccountId;

    @Column(name = "test_user")
    private Boolean testUser;

    @OneToOne
    @JoinColumn(name = "document_image_id")
    private Document documentImage;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_image_request_state")
    private DocumentRequestState documentImageRequestState;
    
    @Column(name = "seen_walk_through")
    private Boolean seenWalkThrough;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Enumerated(EnumType.STRING)
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

    @Column(name = "index_data")
    private String indexData;

    @OneToMany(mappedBy = "user")
    private Set<UserRole> userRoles = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<UserSearch> searches = new HashSet<>();

    @Transient
    private List<Scope> scopes;

    @Transient
    private boolean revealEmail;

    @Transient
    private String defaultOrganizationName;

    @Transient
    private Location defaultLocation;

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
        this.emailDisplay = BoardUtils.obfuscateEmail(email);
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

    public User setPasswordResetUuid(String passwordResetUuid) {
        this.passwordResetUuid = passwordResetUuid;
        return this;
    }

    public LocalDateTime getPasswordResetTimestamp() {
        return passwordResetTimestamp;
    }

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

    public Boolean getTestUser() {
        return testUser;
    }

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

    public String getIndexData() {
        return indexData;
    }

    public User setIndexData(String indexData) {
        this.indexData = indexData;
        return this;
    }

    public Set<UserRole> getUserRoles() {
        return userRoles;
    }

    public Set<UserSearch> getSearches() {
        return searches;
    }

    public List<Scope> getScopes() {
        return scopes;
    }

    public User setScopes(List<Scope> scopes) {
        this.scopes = scopes;
        return this;
    }

    public boolean isRevealEmail() {
        return revealEmail;
    }

    public User setRevealEmail(boolean revealEmail) {
        this.revealEmail = revealEmail;
        return this;
    }

    public String getDefaultOrganizationName() {
        return defaultOrganizationName;
    }

    public User setDefaultOrganizationName(String defaultOrganizationName) {
        this.defaultOrganizationName = defaultOrganizationName;
        return this;
    }

    public Location getDefaultLocation() {
        return defaultLocation;
    }

    public User setDefaultLocation(Location defaultLocation) {
        this.defaultLocation = defaultLocation;
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

    @Override
    public String toString() {
        return Joiner.on(" ").skipNulls().join(givenName, surname, email);
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public int compareTo(User other) {
        int compare = ObjectUtils.compare(givenName, other.getGivenName());
        compare = compare == 0 ? ObjectUtils.compare(surname, other.getSurname()) : compare;
        return compare == 0 ? ObjectUtils.compare(getId(), other.getId()) : compare;
    }

}
