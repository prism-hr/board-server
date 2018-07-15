package hr.prism.board.domain;

import hr.prism.board.enums.AgeRange;
import hr.prism.board.enums.Gender;
import hr.prism.board.enums.MemberCategory;
import hr.prism.board.enums.Role;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static hr.prism.board.utils.BoardUtils.makeSoundex;
import static javax.persistence.EnumType.STRING;

@Entity
@Table(name = "resource_event",
    uniqueConstraints = @UniqueConstraint(columnNames = {"resource_id", "event", "user_id", "ip_address", "role"}))
public class ResourceEvent extends BoardEntity {

    @ManyToOne
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    @Enumerated(STRING)
    @Column(name = "event", nullable = false)
    private hr.prism.board.enums.ResourceEvent event;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "ip_address")
    private String ipAddress;

    @Enumerated(STRING)
    @Column(name = "role")
    private Role role;

    @Column(name = "referral", unique = true)
    private String referral;

    @Enumerated(STRING)
    @Column(name = "gender")
    private Gender gender;

    @Enumerated(STRING)
    @Column(name = "age_range")
    private AgeRange ageRange;

    @ManyToOne
    @JoinColumn(name = "location_nationality_id")
    private Location locationNationality;

    @Enumerated(STRING)
    @Column(name = "member_category")
    private MemberCategory memberCategory;

    @Column(name = "member_program")
    private String memberProgram;

    @Column(name = "member_year")
    private Integer memberYear;

    @ManyToOne
    @JoinColumn(name = "document_resume_id")
    private Document documentResume;

    @Column(name = "website_resume")
    private String websiteResume;

    @Column(name = "covering_note")
    private String coveringNote;

    @Column(name = "index_data")
    private String indexData;

    @OneToOne(mappedBy = "resourceEvent")
    private Activity activity;

    @OneToMany(mappedBy = "resourceEvent")
    private Set<ResourceEventSearch> searches = new HashSet<>();

    @Transient
    private boolean exposeResponseData;

    @Transient
    private boolean viewed;

    public Resource getResource() {
        return resource;
    }

    public ResourceEvent setResource(Resource resource) {
        this.resource = resource;
        return this;
    }

    public hr.prism.board.enums.ResourceEvent getEvent() {
        return event;
    }

    public ResourceEvent setEvent(hr.prism.board.enums.ResourceEvent event) {
        this.event = event;
        return this;
    }

    public User getUser() {
        return user;
    }

    public ResourceEvent setUser(User user) {
        this.user = user;
        return this;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    @SuppressWarnings("UnusedReturnValue")
    public ResourceEvent setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
        return this;
    }

    public Role getRole() {
        return role;
    }

    public ResourceEvent setRole(Role role) {
        this.role = role;
        return this;
    }

    public String getReferral() {
        return referral;
    }

    public ResourceEvent setReferral(String referral) {
        this.referral = referral;
        return this;
    }

    public Gender getGender() {
        return gender;
    }

    public ResourceEvent setGender(Gender gender) {
        this.gender = gender;
        return this;
    }

    public AgeRange getAgeRange() {
        return ageRange;
    }

    public ResourceEvent setAgeRange(AgeRange ageRange) {
        this.ageRange = ageRange;
        return this;
    }

    public Location getLocationNationality() {
        return locationNationality;
    }

    public ResourceEvent setLocationNationality(Location locationNationality) {
        this.locationNationality = locationNationality;
        return this;
    }

    public MemberCategory getMemberCategory() {
        return memberCategory;
    }

    public ResourceEvent setMemberCategory(MemberCategory memberCategory) {
        this.memberCategory = memberCategory;
        return this;
    }

    public String getMemberProgram() {
        return memberProgram;
    }

    public ResourceEvent setMemberProgram(String memberProgram) {
        this.memberProgram = memberProgram;
        return this;
    }

    public Integer getMemberYear() {
        return memberYear;
    }

    public ResourceEvent setMemberYear(Integer memberYear) {
        this.memberYear = memberYear;
        return this;
    }

    public Document getDocumentResume() {
        return documentResume;
    }

    public ResourceEvent setDocumentResume(Document documentResume) {
        this.documentResume = documentResume;
        return this;
    }

    public String getWebsiteResume() {
        return websiteResume;
    }

    public ResourceEvent setWebsiteResume(String websiteResume) {
        this.websiteResume = websiteResume;
        return this;
    }

    public String getCoveringNote() {
        return coveringNote;
    }

    public ResourceEvent setCoveringNote(String coveringNote) {
        this.coveringNote = coveringNote;
        return this;
    }

    @SuppressWarnings("unused")
    public String getIndexData() {
        return indexData;
    }

    public Activity getActivity() {
        return activity;
    }

    public ResourceEvent setActivity(Activity activity) {
        this.activity = activity;
        return this;
    }

    @SuppressWarnings("unused")
    public Set<ResourceEventSearch> getSearches() {
        return searches;
    }

    public boolean isExposeResponseData() {
        return exposeResponseData;
    }

    public ResourceEvent setExposeResponseData(boolean exposeResponseData) {
        this.exposeResponseData = exposeResponseData;
        return this;
    }

    public boolean isViewed() {
        return viewed;
    }

    public ResourceEvent setViewed(boolean viewed) {
        this.viewed = viewed;
        return this;
    }

    public ResourceEvent setIndexData() {
        this.indexData = makeSoundex(
            newArrayList(gender, ageRange, locationNationality, memberCategory, memberProgram, memberYear));
        return this;
    }

}
