package hr.prism.board.domain;

import hr.prism.board.enums.AgeRange;
import hr.prism.board.enums.Gender;
import hr.prism.board.enums.MemberCategory;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "resource_event")
public class ResourceEvent extends BoardEntity {

    @ManyToOne
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    @Enumerated(EnumType.STRING)
    @Column(name = "event", nullable = false)
    private hr.prism.board.enums.ResourceEvent event;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "referral", unique = true)
    private String referral;

    @Enumerated
    @Column(name = "gender")
    private Gender gender;

    @Enumerated
    @Column(name = "age_range")
    private AgeRange ageRange;

    @ManyToOne
    @JoinColumn(name = "location_nationality_id")
    private Location locationNationality;

    @Enumerated(EnumType.STRING)
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

    @Transient
    private boolean exposeResponseData;

    @Transient
    private boolean viewed;

    @Transient
    private List<ResourceEvent> history;

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

    public ResourceEvent setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
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

    public String getIndexData() {
        return indexData;
    }

    public ResourceEvent setIndexData(String indexData) {
        this.indexData = indexData;
        return this;
    }

    public Activity getActivity() {
        return activity;
    }

    public ResourceEvent setActivity(Activity activity) {
        this.activity = activity;
        return this;
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

    public List<ResourceEvent> getHistory() {
        return history;
    }

    public ResourceEvent setHistory(List<ResourceEvent> history) {
        this.history = history;
        return this;
    }

}
