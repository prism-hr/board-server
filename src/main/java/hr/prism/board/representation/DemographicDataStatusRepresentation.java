package hr.prism.board.representation;

import hr.prism.board.enums.MemberCategory;

import java.time.LocalDate;

public class DemographicDataStatusRepresentation {

    private boolean requireUserData;

    private boolean requireMemberData;

    private MemberCategory memberCategory;

    private String memberProgram;

    private Integer memberYear;

    private LocalDate expiryDate;

    public boolean isRequireUserData() {
        return requireUserData;
    }

    public DemographicDataStatusRepresentation setRequireUserData(boolean requireUserData) {
        this.requireUserData = requireUserData;
        return this;
    }

    public boolean isRequireMemberData() {
        return requireMemberData;
    }

    public DemographicDataStatusRepresentation setRequireMemberData(boolean requireMemberData) {
        this.requireMemberData = requireMemberData;
        return this;
    }

    public MemberCategory getMemberCategory() {
        return memberCategory;
    }

    public DemographicDataStatusRepresentation setMemberCategory(MemberCategory memberCategory) {
        this.memberCategory = memberCategory;
        return this;
    }

    public String getMemberProgram() {
        return memberProgram;
    }

    public DemographicDataStatusRepresentation setMemberProgram(String memberProgram) {
        this.memberProgram = memberProgram;
        return this;
    }

    public Integer getMemberYear() {
        return memberYear;
    }

    public DemographicDataStatusRepresentation setMemberYear(Integer memberYear) {
        this.memberYear = memberYear;
        return this;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public DemographicDataStatusRepresentation setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
        return this;
    }

}
