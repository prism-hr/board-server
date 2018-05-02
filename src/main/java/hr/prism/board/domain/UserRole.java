package hr.prism.board.domain;

import hr.prism.board.enums.MemberCategory;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.State;
import hr.prism.board.value.Statistics;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static javax.persistence.EnumType.STRING;

@Entity
@NamedEntityGraph(
    name = "userRole.extended",
    attributeNodes = {
        @NamedAttributeNode(value = "resource"),
        @NamedAttributeNode(value = "user")})
@NamedNativeQuery(
    name = "memberStatistics",
    query =
        "SELECT COALESCE(SUM(IF(user_role.expiry_date IS NULL " +
            "OR user_role.expiry_date >= CURRENT_DATE(), 1, 0)), 0) as countLive, " +
            "COALESCE(SUM(IF(user_role.created_timestamp >= MAKEDATE(YEAR(CURRENT_DATE()) - IF(" +
            "MONTH(CURRENT_DATE()) > 9, 0, 1), 10), 1, 0)), 0) AS countThisYear, " +
            "COUNT(user_role.id) as countAllTime, " +
            "MAX(IF(user_role.expiry_date IS NULL " +
            "OR user_role.expiry_date >= CURRENT_DATE(), user_role.created_timestamp, NULL)) as mostRecent " +
            "FROM user_role " +
            "WHERE user_role.resource_id = :departmentId " +
            "AND user_role.role = 'MEMBER' " +
            "AND user_role.state IN ('PENDING', 'ACCEPTED')",
    resultSetMapping = "memberStatistics")
@SqlResultSetMapping(
    name = "memberStatistics",
    classes = @ConstructorResult(
        targetClass = Statistics.class,
        columns = {
            @ColumnResult(name = "countLive", type = Long.class),
            @ColumnResult(name = "countThisYear", type = Long.class),
            @ColumnResult(name = "countAllTime", type = Long.class),
            @ColumnResult(name = "mostRecent", type = LocalDateTime.class)}))
@Table(name = "user_role", uniqueConstraints = @UniqueConstraint(columnNames = {"resource_id", "user_id", "role"}))
@SuppressWarnings("SqlResolve")
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

    @Enumerated(STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Enumerated(STRING)
    @Column(name = "member_category")
    private MemberCategory memberCategory;

    @Column(name = "member_program")
    private String memberProgram;

    @Column(name = "member_year")
    private Integer memberYear;

    @Column(name = "member_date")
    private LocalDate memberDate;

    @Enumerated(STRING)
    @Column(name = "state", nullable = false)
    private State state;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @OneToOne(mappedBy = "userRole")
    private Activity activity;

    @Transient
    private boolean viewed;

    @Transient
    private boolean created;

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

    public boolean isCreated() {
        return created;
    }

    public UserRole setCreated(boolean created) {
        this.created = created;
        return this;
    }

}
