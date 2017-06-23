package hr.prism.board.service.cache;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.domain.UserRole;
import hr.prism.board.domain.UserRoleCategory;
import hr.prism.board.dto.ResourceUserDTO;
import hr.prism.board.dto.UserRoleDTO;
import hr.prism.board.enums.CategoryType;
import hr.prism.board.enums.MemberCategory;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.Scope;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.repository.UserRoleCategoryRepository;
import hr.prism.board.repository.UserRoleRepository;
import hr.prism.board.service.ResourceService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.IntStream;

@Service
@Transactional
public class UserRoleCacheService {

    @Inject
    private UserRoleRepository userRoleRepository;

    @Inject
    private UserRoleCategoryRepository userRoleCategoryRepository;

    @Inject
    private ResourceService resourceService;

    @PersistenceContext
    private EntityManager entityManager;

    @CacheEvict(key = "#user.id", value = "users")
    public void createUserRole(Resource resource, User user, UserRoleDTO userRoleDTO) {
        UserRole userRole = userRoleRepository.save(
            new UserRole().setResource(resource).setUser(user).setRole(userRoleDTO.getRole()).setExpiryDate(userRoleDTO.getExpiryDate()));

        List<MemberCategory> newCategories = userRoleDTO.getCategories();
        if (userRoleDTO.getRole() == Role.MEMBER) {
            Resource department = resourceService.findByResourceAndEnclosingScope(resource, Scope.DEPARTMENT);
            resourceService.validateCategories(department, CategoryType.MEMBER, MemberCategory.toStrings(newCategories),
                ExceptionCode.MISSING_USER_ROLE_MEMBER_CATEGORIES,
                ExceptionCode.INVALID_USER_ROLE_MEMBER_CATEGORIES,
                ExceptionCode.CORRUPTED_USER_ROLE_MEMBER_CATEGORIES);

            IntStream.range(0, newCategories.size())
                .forEach(index -> {
                    MemberCategory newCategory = newCategories.get(index);
                    UserRoleCategory userRoleCategory = new UserRoleCategory();
                    userRoleCategory.setUserRole(userRole);
                    userRoleCategory.setName(newCategory);
                    userRoleCategory.setOrdinal(index);
                    userRoleCategoryRepository.save(userRoleCategory);
                });
        }
    }

    @CacheEvict(key = "#user.id", value = "users")
    public void deleteResourceUser(Resource resource, User user) {
        userRoleCategoryRepository.deleteByResourceAndUser(resource, user);
        userRoleRepository.deleteByResourceAndUser(resource, user);
        checkSafety(resource, ExceptionCode.IRREMOVABLE_USER);
    }

    @CacheEvict(key = "#user.id", value = "users")
    public void updateResourceUser(Resource resource, User user, ResourceUserDTO resourceUserDTO) {
        if (resourceUserDTO.getRoles().isEmpty()) {
            throw new BoardException(ExceptionCode.IRREMOVABLE_USER_ROLE);
        }

        userRoleCategoryRepository.deleteByResourceAndUser(resource, user);
        userRoleRepository.deleteByResourceAndUser(resource, user);
        entityManager.flush();

        for (UserRoleDTO userRoleDTO : resourceUserDTO.getRoles()) {
            createUserRole(resource, user, userRoleDTO);
        }

        checkSafety(resource, ExceptionCode.IRREMOVABLE_USER_ROLE);
    }

    private void checkSafety(Resource resource, ExceptionCode exceptionCode) {
        if (resource.getScope() == Scope.DEPARTMENT) {
            List<UserRole> remainingAdminRoles = userRoleRepository.findByResourceAndRole(resource, Role.ADMINISTRATOR);
            if (remainingAdminRoles.isEmpty()) {
                throw new BoardException(exceptionCode);
            }
        }
    }

}
