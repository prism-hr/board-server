package hr.prism.board.representation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import hr.prism.board.enums.*;

import java.time.LocalDateTime;

@JsonIgnoreProperties({"resourceId", "userId", "role", "event"})
public class ActivityRepresentation {

    private Long id;

    private Activity activity;

    private String image;

    private Long resourceId;

    private String handle;

    private String department;

    private String board;

    private String post;

    private Long userId;

    private Role role;

    private String givenName;

    private String surname;

    private ResourceEvent event;

    private Gender gender;

    private AgeRange ageRange;

    private String location;

    private Boolean viewed;

    private LocalDateTime created;

    public Long getId() {
        return id;
    }

    public ActivityRepresentation setId(Long id) {
        this.id = id;
        return this;
    }

    public Activity getActivity() {
        return activity;
    }

    public ActivityRepresentation setActivity(Activity activity) {
        this.activity = activity;
        return this;
    }

    public String getImage() {
        return image;
    }

    public ActivityRepresentation setImage(String image) {
        this.image = image;
        return this;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public ActivityRepresentation setResourceId(Long resourceId) {
        this.resourceId = resourceId;
        return this;
    }

    public String getHandle() {
        return handle;
    }

    public ActivityRepresentation setHandle(String handle) {
        this.handle = handle;
        return this;
    }

    public String getDepartment() {
        return department;
    }

    public ActivityRepresentation setDepartment(String department) {
        this.department = department;
        return this;
    }

    public String getBoard() {
        return board;
    }

    public ActivityRepresentation setBoard(String board) {
        this.board = board;
        return this;
    }

    public String getPost() {
        return post;
    }

    public ActivityRepresentation setPost(String post) {
        this.post = post;
        return this;
    }

    public Long getUserId() {
        return userId;
    }

    public ActivityRepresentation setUserId(Long userId) {
        this.userId = userId;
        return this;
    }

    public Role getRole() {
        return role;
    }

    public ActivityRepresentation setRole(Role role) {
        this.role = role;
        return this;
    }

    public String getGivenName() {
        return givenName;
    }

    public ActivityRepresentation setGivenName(String givenName) {
        this.givenName = givenName;
        return this;
    }

    public String getSurname() {
        return surname;
    }

    public ActivityRepresentation setSurname(String surname) {
        this.surname = surname;
        return this;
    }

    public ResourceEvent getEvent() {
        return event;
    }

    public ActivityRepresentation setEvent(ResourceEvent event) {
        this.event = event;
        return this;
    }

    public Gender getGender() {
        return gender;
    }

    public ActivityRepresentation setGender(Gender gender) {
        this.gender = gender;
        return this;
    }

    public AgeRange getAgeRange() {
        return ageRange;
    }

    public ActivityRepresentation setAgeRange(AgeRange ageRange) {
        this.ageRange = ageRange;
        return this;
    }

    public String getLocation() {
        return location;
    }

    public ActivityRepresentation setLocation(String location) {
        this.location = location;
        return this;
    }

    public Boolean getViewed() {
        return viewed;
    }

    public ActivityRepresentation setViewed(Boolean viewed) {
        this.viewed = viewed;
        return this;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public ActivityRepresentation setCreated(LocalDateTime created) {
        this.created = created;
        return this;
    }

}
