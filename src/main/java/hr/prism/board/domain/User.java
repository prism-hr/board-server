package hr.prism.board.domain;

import com.google.common.base.Joiner;
import hr.prism.board.enums.DocumentRequestState;
import hr.prism.board.enums.OauthProvider;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Email;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user")
public class User extends BoardEntity implements Comparable<User> {
    
    @Column(name = "given_name", nullable = false)
    private String givenName;
    
    @Column(name = "surname", nullable = false)
    private String surname;
    
    @Email
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    
    @Column(name = "password")
    private String password;
    
    @Column(name = "temporary_password")
    private String temporaryPassword;
    
    @Column(name = "temporary_password_expiry_timestamp")
    private LocalDateTime temporaryPasswordExpiryTimestamp;
    
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
    
    @OneToMany(mappedBy = "user")
    private Set<UserRole> userRoles = new HashSet<>();
    
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
        return this;
    }
    
    public String getPassword() {
        return password;
    }
    
    public User setPassword(String password) {
        this.password = password;
        return this;
    }
    
    public String getTemporaryPassword() {
        return temporaryPassword;
    }
    
    public User setTemporaryPassword(String temporaryPassword) {
        this.temporaryPassword = temporaryPassword;
        return this;
    }
    
    public LocalDateTime getTemporaryPasswordExpiryTimestamp() {
        return temporaryPasswordExpiryTimestamp;
    }
    
    public User setTemporaryPasswordExpiryTimestamp(LocalDateTime temporaryPasswordExpiryTimestamp) {
        this.temporaryPasswordExpiryTimestamp = temporaryPasswordExpiryTimestamp;
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
    
    public Set<UserRole> getUserRoles() {
        return userRoles;
    }
    
    public User setUserRoles(Set<UserRole> userRoles) {
        this.userRoles = userRoles;
        return this;
    }
    
    public String getFullName() {
        return Joiner.on(StringUtils.SPACE).join(givenName, surname);
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
