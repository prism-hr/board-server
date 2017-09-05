package hr.prism.board.domain;

import com.google.common.base.Joiner;
import hr.prism.board.enums.DocumentRequestState;
import hr.prism.board.enums.OauthProvider;
import org.apache.commons.lang3.ObjectUtils;
import org.hibernate.validator.constraints.Email;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user")
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

    @Email
    @Column(name = "email_original", nullable = false, unique = true)
    private String emailOriginal;

    @Column(name = "password")
    private String password;

    @Column(name = "password_reset_uuid")
    private String passwordResetUuid;

    @Column(name = "password_reset_timestamp")
    private LocalDateTime passwordResetTimestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "oauth_provider")
    private OauthProvider oauthProvider;

    @Column(name = "oauth_account_id")
    private String oauthAccountId;

    @OneToOne
    @JoinColumn(name = "document_image_id")
    private Document documentImage;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_image_request_state")
    private DocumentRequestState documentImageRequestState;

    @OneToOne
    @JoinColumn(name = "document_resume_id")
    private Document documentResume;

    @Column(name = "website_resume")
    private String websiteResume;

    @Column(name = "indexData")
    private String indexData;

    @OneToMany(mappedBy = "user")
    private Set<UserRole> userRoles = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<UserSearch> searches = new HashSet<>();

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
        if (this.emailOriginal == null) {
            this.emailOriginal = email;
        }

        return this;
    }

    public String getEmailOriginal() {
        return emailOriginal;
    }

    public User setEmailOriginal(String emailOriginal) {
        this.emailOriginal = emailOriginal;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public User setPassword(String password) {
        this.password = password;
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

    public String getFullName() {
        return Joiner.on(" ").skipNulls().join(givenName, surname);
    }

    @Override
    public String toString() {
        return Joiner.on(" ").skipNulls().join(givenName, surname, email);
    }

    @Override
    public int compareTo(User other) {
        int compare = ObjectUtils.compare(givenName, other.getGivenName());
        compare = compare == 0 ? ObjectUtils.compare(surname, other.getSurname()) : compare;
        return compare == 0 ? ObjectUtils.compare(getId(), other.getId()) : compare;
    }

}
