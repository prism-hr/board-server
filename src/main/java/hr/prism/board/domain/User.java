package hr.prism.board.domain;

import com.google.common.base.Joiner;
import org.hibernate.validator.constraints.Email;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user")
public class User extends BoardEntity {
    
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
    
    @Column(name = "stormpath_id", nullable = false, unique = true)
    private String stormpathId;
    
    @OneToOne
    @JoinColumn(name = "document_image_id")
    private Document documentImage;
    
    @OneToMany(mappedBy = "user")
    private Set<UserRole> userRoles = new HashSet<>();
    
    @Transient
    private String accessToken;
    
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
    
    public void setUserRoles(Set<UserRole> userRoles) {
        this.userRoles = userRoles;
    }
    
    public String getStormpathId() {
        return stormpathId;
    }
    
    public User setStormpathId(String stormpathId) {
        this.stormpathId = stormpathId;
        return this;
    }
    
    public Document getDocumentImage() {
        return documentImage;
    }
    
    public User setDocumentImage(Document documentImage) {
        this.documentImage = documentImage;
        return this;
    }
    
    public Set<UserRole> getUserRoles() {
        return userRoles;
    }
    
    @Override
    public String toString() {
        return Joiner.on(" ").skipNulls().join(givenName, surname, email);
    }
    
}
