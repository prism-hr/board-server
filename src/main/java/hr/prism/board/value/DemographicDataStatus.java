package hr.prism.board.value;

import hr.prism.board.enums.MemberCategory;

import java.time.LocalDate;

public class DemographicDataStatus {

    private boolean requireUserData;

    private boolean requireMemberData;

    private MemberCategory memberCategory;

    private String memberProgram;

    private Integer memberYear;

    private LocalDate expiryDate;

    public boolean isRequireUserData() {
        return requireUserData;
    }

    public DemographicDataStatus setRequireUserData(boolean requireUserData) {
        this.requireUserData = requireUserData;
        return this;
    }

    public boolean isRequireMemberData() {
        return requireMemberData;
    }

    public DemographicDataStatus setRequireMemberData(boolean requireMemberData) {
        this.requireMemberData = requireMemberData;
        return this;
    }

    public MemberCategory getMemberCategory() {
        return memberCategory;
    }

    public DemographicDataStatus setMemberCategory(MemberCategory memberCategory) {
        this.memberCategory = memberCategory;
        return this;
    }

    public String getMemberProgram() {
        return memberProgram;
    }

    public DemographicDataStatus setMemberProgram(String memberProgram) {
        this.memberProgram = memberProgram;
        return this;
    }

    public Integer getMemberYear() {
        return memberYear;
    }

    public DemographicDataStatus setMemberYear(Integer memberYear) {
        this.memberYear = memberYear;
        return this;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public DemographicDataStatus setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
        return this;
    }

    public boolean isReady() {
        return !(requireUserData || requireMemberData);
    }

}
