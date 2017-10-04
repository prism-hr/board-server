package hr.prism.board.domain;

import hr.prism.board.enums.MemberCategory;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.State;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@NamedEntityGraph(
    name = "userRole.extended",
    attributeNodes = {
        @NamedAttributeNode(value = "resource"),
        @NamedAttributeNode(value = "user")})
@Table(name = "user_role", uniqueConstraints = @UniqueConstraint(columnNames = {"resource_id", "user_id", "role"}))
public class UserRole extends BoardEntity {

    @Column(name = "uuid")
    private String uuid;

    @ManyToOne
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "email")
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_category")
    private MemberCategory memberCategory;

    @Column(name = "member_program")
    private String memberProgram;

    @Column(name = "member_year")
    private Integer memberYear;

    @Column(name = "member_date")
    private LocalDate memberDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private State state;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @OneToOne(mappedBy = "userRole")
    private Activity activity;

    @Transient
    private boolean viewed;

    public String getUuid() {
        return uuid;
    }

    public UserRole setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public Resource getResource() {
        return resource;
    }

    public UserRole setResource(Resource resource) {
        this.resource = resource;
        return this;
    }

    public User getUser() {
        return user;
    }

    public UserRole setUser(User user) {
        this.user = user;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public UserRole setEmail(String email) {
        this.email = email;
        return this;
    }

    public Role getRole() {
        return role;
    }

    public UserRole setRole(Role role) {
        this.role = role;
        return this;
    }

    public MemberCategory getMemberCategory() {
        return memberCategory;
    }

    public UserRole setMemberCategory(MemberCategory memberCategory) {
        this.memberCategory = memberCategory;
        return this;
    }

    public String getMemberProgram() {
        return memberProgram;
    }

    public UserRole setMemberProgram(String memberProgram) {
        this.memberProgram = memberProgram;
        return this;
    }

    public Integer getMemberYear() {
        return memberYear;
    }

    public UserRole setMemberYear(Integer memberYear) {
        this.memberYear = memberYear;
        return this;
    }

    public LocalDate getMemberDate() {
        return memberDate;
    }

    public UserRole setMemberDate(LocalDate memberDate) {
        this.memberDate = memberDate;
        return this;
    }

    public State getState() {
        return state;
    }

    public UserRole setState(State state) {
        this.state = state;
        return this;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public UserRole setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
        return this;
    }

    public Activity getActivity() {
        return activity;
    }

    public UserRole setActivity(Activity activity) {
        this.activity = activity;
        return this;
    }

    public boolean isViewed() {
        return viewed;
    }

    public UserRole setViewed(boolean viewed) {
        this.viewed = viewed;
        return this;
    }

}
