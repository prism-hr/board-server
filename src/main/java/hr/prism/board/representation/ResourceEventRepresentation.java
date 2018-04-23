package hr.prism.board.representation;

import hr.prism.board.enums.AgeRange;
import hr.prism.board.enums.Gender;
import hr.prism.board.enums.MemberCategory;
import hr.prism.board.enums.ResourceEvent;

import java.time.LocalDateTime;

public class ResourceEventRepresentation {

    private Long id;

    private ResourceEvent event;

    private UserRepresentation user;

    private String ipAddress;

    private String referral;

    private Gender gender;

    private AgeRange ageRange;

    private LocationRepresentation locationNationality;

    private MemberCategory memberCategory;

    private String memberProgram;

    private Integer memberYear;

    private DocumentRepresentation documentResume;

    private String websiteResume;

    private String coveringNote;

    private LocalDateTime createdTimestamp;

    private boolean viewed;

    public Long getId() {
        return id;
    }

    public ResourceEventRepresentation setId(Long id) {
        this.id = id;
        return this;
    }

    public ResourceEvent getEvent() {
        return event;
    }

    public ResourceEventRepresentation setEvent(ResourceEvent event) {
        this.event = event;
        return this;
    }

    public UserRepresentation getUser() {
        return user;
    }

    public ResourceEventRepresentation setUser(UserRepresentation user) {
        this.user = user;
        return this;
    }

    @SuppressWarnings("unused")
    public String getIpAddress() {
        return ipAddress;
    }

    @SuppressWarnings("UnusedReturnValue")
    public ResourceEventRepresentation setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
        return this;
    }

    public String getReferral() {
        return referral;
    }

    public ResourceEventRepresentation setReferral(String referral) {
        this.referral = referral;
        return this;
    }

    public Gender getGender() {
        return gender;
    }

    public ResourceEventRepresentation setGender(Gender gender) {
        this.gender = gender;
        return this;
    }

    public AgeRange getAgeRange() {
        return ageRange;
    }

    public ResourceEventRepresentation setAgeRange(AgeRange ageRange) {
        this.ageRange = ageRange;
        return this;
    }

    public LocationRepresentation getLocationNationality() {
        return locationNationality;
    }

    public ResourceEventRepresentation setLocationNationality(LocationRepresentation locationNationality) {
        this.locationNationality = locationNationality;
        return this;
    }

    public MemberCategory getMemberCategory() {
        return memberCategory;
    }

    public ResourceEventRepresentation setMemberCategory(MemberCategory memberCategory) {
        this.memberCategory = memberCategory;
        return this;
    }

    public String getMemberProgram() {
        return memberProgram;
    }

    public ResourceEventRepresentation setMemberProgram(String memberProgram) {
        this.memberProgram = memberProgram;
        return this;
    }

    public Integer getMemberYear() {
        return memberYear;
    }

    public ResourceEventRepresentation setMemberYear(Integer memberYear) {
        this.memberYear = memberYear;
        return this;
    }

    @SuppressWarnings("unused")
    public DocumentRepresentation getDocumentResume() {
        return documentResume;
    }

    public ResourceEventRepresentation setDocumentResume(DocumentRepresentation documentResume) {
        this.documentResume = documentResume;
        return this;
    }

    public String getWebsiteResume() {
        return websiteResume;
    }

    public ResourceEventRepresentation setWebsiteResume(String websiteResume) {
        this.websiteResume = websiteResume;
        return this;
    }

    @SuppressWarnings("unused")
    public String getCoveringNote() {
        return coveringNote;
    }

    @SuppressWarnings("UnusedReturnValue")
    public ResourceEventRepresentation setCoveringNote(String coveringNote) {
        this.coveringNote = coveringNote;
        return this;
    }

    @SuppressWarnings("unused")
    public LocalDateTime getCreatedTimestamp() {
        return createdTimestamp;
    }

    public ResourceEventRepresentation setCreatedTimestamp(LocalDateTime createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
        return this;
    }

    public boolean isViewed() {
        return viewed;
    }

    public ResourceEventRepresentation setViewed(boolean viewed) {
        this.viewed = viewed;
        return this;
    }

}
