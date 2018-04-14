package hr.prism.board.representation;

public class DemographicDataStatusRepresentation {

    // true: post / put with user demographic data (gender, age range, home country)
    // false: user demographic data already complete, skip this step
    private boolean requireUserDemographicData;

    // true: post / put with user role demographic data (member category, member program, member year)
    // false: user role demographic data not required or already provided, skip this step
    private boolean requireUserRoleDemographicData;

    private UserRoleRepresentation userRole;

    public boolean isRequireUserDemographicData() {
        return requireUserDemographicData;
    }

    public DemographicDataStatusRepresentation setRequireUserDemographicData(boolean requireUserDemographicData) {
        this.requireUserDemographicData = requireUserDemographicData;
        return this;
    }

    public boolean isRequireUserRoleDemographicData() {
        return requireUserRoleDemographicData;
    }

    public DemographicDataStatusRepresentation setRequireUserRoleDemographicData(
        boolean requireUserRoleDemographicData) {
        this.requireUserRoleDemographicData = requireUserRoleDemographicData;
        return this;
    }

    public UserRoleRepresentation getUserRole() {
        return userRole;
    }

    public DemographicDataStatusRepresentation setUserRole(UserRoleRepresentation userRole) {
        this.userRole = userRole;
        return this;
    }

    public boolean isReady() {
        return !requireUserDemographicData && !requireUserRoleDemographicData;
    }

}