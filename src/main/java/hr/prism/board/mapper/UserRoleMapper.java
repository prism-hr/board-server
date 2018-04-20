package hr.prism.board.mapper;

import hr.prism.board.domain.User;
import hr.prism.board.domain.UserRole;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.RoleType;
import hr.prism.board.representation.*;
import hr.prism.board.value.UserRoles;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

@Component
public class UserRoleMapper implements Function<UserRole, UserRoleRepresentation> {

    private final UserMapper userMapper;

    @Inject
    public UserRoleMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public UserRoleRepresentation apply(UserRole userRole) {
        if (userRole == null) {
            return null;
        }

        return new UserRoleRepresentation()
            .setUser(userMapper.apply(userRole.getUser()))
            .setEmail(userRole.getEmail())
            .setRole(userRole.getRole())
            .setState(userRole.getState())
            .setMemberCategory(userRole.getMemberCategory())
            .setMemberProgram(userRole.getMemberProgram())
            .setMemberYear(userRole.getMemberYear())
            .setExpiryDate(userRole.getExpiryDate())
            .setViewed(userRole.isViewed())
            .setCreatedTimestamp(userRole.getCreatedTimestamp())
            .setUpdatedTimestamp(userRole.getUpdatedTimestamp());
    }

    public NewUserRoleRepresentation<?> apply(List<UserRole> userRoles) {
        if (isEmpty(userRoles)) {
            return null;
        }

        UserRole userRole0 = userRoles.get(0);
        RoleType type = userRole0.getRole().getType();
        switch (type) {
            case STAFF:
                User user = userRole0.getUser();
                List<Role> roles =
                    userRoles.stream()
                        .map(UserRole::getRole)
                        .collect(toList());

                return applyStaff(user, roles);
            case MEMBER:
                return applyMember(userRole0);
            default:
                throw new IllegalStateException("Unexpected role type: " + type);
        }
    }

    public UserRolesRepresentation apply(UserRoles userRoles) {
        if (userRoles == null) {
            return null;
        }

        return new UserRolesRepresentation()
            .setStaff(applyStaff(userRoles.getStaff()))
            .setMembers(userRoles.getMembers().stream().map(this::applyMember).collect(toList()))
            .setMemberRequests(userRoles.getMemberRequests().stream().map(this::applyMember).collect(toList()))
            .setMemberToBeUploadedCount(userRoles.getMemberToBeUploadedCount());
    }

    private StaffRepresentation applyStaff(User user, List<Role> roles) {
        return new StaffRepresentation()
            .setUser(userMapper.apply(user))
            .setRoles(roles);
    }

    private MemberRepresentation applyMember(UserRole userRole) {
        return new MemberRepresentation()
            .setUser(userMapper.apply(userRole.getUser()))
            .setEmail(userRole.getEmail())
            .setState(userRole.getState())
            .setMemberCategory(userRole.getMemberCategory())
            .setMemberProgram(userRole.getMemberProgram())
            .setMemberYear(userRole.getMemberYear())
            .setExpiryDate(userRole.getExpiryDate())
            .setViewed(userRole.isViewed());
    }

    private List<StaffRepresentation> applyStaff(List<UserRole> userRoles) {
        List<StaffRepresentation> staff = new ArrayList<>();
        LinkedHashMap<User, StaffRepresentation> staffIndex = new LinkedHashMap<>();

        userRoles.forEach(userRole -> {
            User user = userRole.getUser();
            Role role = userRole.getRole();
            StaffRepresentation staffMember = staffIndex.get(user);
            if (staffMember == null) {
                staffMember =
                    new StaffRepresentation()
                        .setUser(userMapper.apply(user))
                        .addRole(role);
                staffIndex.put(user, staffMember);
                staff.add(staffMember);
            } else {
                staffMember.addRole(role);
            }
        });

        return staff;
    }

}
