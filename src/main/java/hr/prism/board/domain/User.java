package hr.prism.board.domain;

import com.google.common.base.Joiner;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user")
public class User extends BoardEntity {
    
    @Column(name = "given_name", nullable = false)
    private String givenName;
    
    @Column(name = "surname", nullable = false)
    private String surname;
    
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    
    @Column(name = "stormpath_id", nullable = false, unique = true)
    private String stormpathId;
    
    @OneToMany(mappedBy = "user")
    private Set<UserRole> userRoles = new HashSet<>();
    
    public String getGivenName() {
        return givenName;
    }
    
    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }
    
    public String getSurname() {
        return surname;
    }
    
    public void setSurname(String surname) {
        this.surname = surname;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getStormpathId() {
        return stormpathId;
    }
    
    public void setStormpathId(String stormpathId) {
        this.stormpathId = stormpathId;
    }
    
    public Set<UserRole> getUserRoles() {
        return userRoles;
    }
    
    @Override
    public String toString() {
        return Joiner.on(" ").skipNulls().join(givenName, surname, email);
    }
    
}
