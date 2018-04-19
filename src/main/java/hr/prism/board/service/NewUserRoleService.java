package hr.prism.board.service;

import hr.prism.board.dao.UserRoleDAO;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.domain.UserRole;
import hr.prism.board.dto.MemberDTO;
import hr.prism.board.enums.MemberCategory;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.State;
import hr.prism.board.repository.UserRoleRepository;
import hr.prism.board.value.Statistics;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.List;

import static hr.prism.board.utils.BoardUtils.emptyToNull;
import static java.util.UUID.randomUUID;

@Service
@Transactional
public class NewUserRoleService {

    private final UserRoleRepository userRoleRepository;

    private final UserRoleDAO userRoleDAO;

    private final NewUserService userService;

    @Inject
    public NewUserRoleService(UserRoleRepository userRoleRepository, UserRoleDAO userRoleDAO,
                              NewUserService userService) {
        this.userRoleRepository = userRoleRepository;
        this.userRoleDAO = userRoleDAO;
        this.userService = userService;
    }

    public UserRole getById(Long id) {
        return userRoleRepository.findOne(id);
    }

    public List<Role> getByResourceAndUser(Resource resource, User user) {
        return userRoleRepository.findByResourceAndUser(resource, user);
    }

    public UserRole getByResourceUserAndRole(Resource resource, User user, Role role) {
        return userRoleRepository.findByResourceAndUserAndRole(resource, user, role);
    }

    public UserRole getByResourceAndUserIdAndRole(Resource resource, Long userId, Role role) {
        return userRoleRepository.findByResourceAndUserIdAndRole(resource, userId, role);
    }

    public UserRole createUserRole(Resource resource, User user, Role role) {
        UserRole userRole =
            new UserRole()
                .setUuid(randomUUID().toString())
                .setResource(resource)
                .setUser(user)
                .setRole(role)
                .setCreated(true);

        userRole.setCreatorId(resource.getCreatorId());
        return userRoleRepository.save(userRole);
    }

    public UserRole createOrUpdateUserRole(Resource resource, User userCreateUpdate, MemberDTO memberDTO, State state) {
        UserRole userRole = getByResourceUserAndRole(resource, userCreateUpdate, Role.MEMBER);
        if (userRole == null) {
            return createUserRole(resource, userCreateUpdate, memberDTO, state);
        }

        userRole = updateMembership(userRole, memberDTO);
        userRole.setState(state);
        return userRole;
    }

    public UserRole updateMembership(UserRole userRole, MemberDTO memberDTO) {
        boolean updated = false;
        boolean clearStudyData = false;

        MemberCategory oldMemberCategory = userRole.getMemberCategory();
        MemberCategory newMemberCategory = memberDTO.getMemberCategory();
        if (newMemberCategory != null) {
            userRole.setMemberCategory(newMemberCategory);
            clearStudyData = newMemberCategory != oldMemberCategory;
            updated = true;
        }

        String memberProgram = memberDTO.getMemberProgram();
        if (memberProgram != null || clearStudyData) {
            userRole.setMemberProgram(memberProgram);
            updated = true;
        }

        Integer memberYear = memberDTO.getMemberYear();
        if (memberYear != null || clearStudyData) {
            userRole.setMemberYear(memberYear);
            updated = true;
        }

        if (updated) {
            userRole.setMemberDate(LocalDate.now());
        }

        return userRole;
    }

    public void deleteUserRole(Resource resource, User user, Role role) {
        userRoleRepository.deleteByResourceAndUserAndRole(resource, user, role);
    }

    public Statistics getMemberStatistics(Long departmentId) {
        return userRoleDAO.getMemberStatistics(departmentId);
    }

    private UserRole getOrCreateUserRole(Resource resource, User userCreateUpdate, Role role) {
        UserRole userRole = userRoleRepository.findByResourceAndUserAndRole(resource, userCreateUpdate, role);
        if (userRole == null) {
            return createUserRole(resource, userCreateUpdate, role);
        }

        return userRole;
    }

    private UserRole createUserRole(Resource resource, User user, MemberDTO memberDTO, State state) {
        UserRole userRole =
            new UserRole()
                .setUuid(randomUUID().toString())
                .setResource(resource)
                .setUser(user)
                .setEmail(emptyToNull(memberDTO.getEmail()))
                .setRole(Role.MEMBER)
                .setMemberCategory(memberDTO.getMemberCategory())
                .setMemberProgram(memberDTO.getMemberProgram())
                .setMemberYear(memberDTO.getMemberYear())
                .setMemberDate(LocalDate.now())
                .setState(state)
                .setExpiryDate(memberDTO.getExpiryDate());

        userRole.setCreatorId(resource.getCreatorId());
        return userRoleRepository.save(userRole);
    }

}
