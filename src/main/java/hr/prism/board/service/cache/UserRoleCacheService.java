package hr.prism.board.service.cache;

import hr.prism.board.domain.*;
import hr.prism.board.dto.UserRoleDTO;
import hr.prism.board.enums.CategoryType;
import hr.prism.board.exception.ApiException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.repository.UserRoleCategoryRepository;
import hr.prism.board.repository.UserRoleRepository;
import hr.prism.board.service.ResourceService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
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

    @CacheEvict(key = "#user.id", value = "users")
    public void createUserRole(Resource resource, User user, UserRoleDTO userRoleDTO) {
        UserRole userRole = new UserRole().setResource(resource).setUser(user).setRole(userRoleDTO.getRole())
            .setExpiryDate(userRoleDTO.getExpiryDate());
        UserRole savedUserRole = userRoleRepository.save(userRole);

        Department department = resource.getDepartment();
        List<String> newCategories = userRoleDTO.getCategories();
        if (userRoleDTO.getRole() == Role.MEMBER) {
            resourceService.validateCategories(department, CategoryType.MEMBER, newCategories,
                ExceptionCode.MISSING_USER_ROLE_MEMBER_CATEGORIES,
                ExceptionCode.INVALID_USER_ROLE_MEMBER_CATEGORIES,
                ExceptionCode.CORRUPTED_USER_ROLE_MEMBER_CATEGORIES);

            IntStream.range(0, newCategories.size())
                .forEach(index -> {
                    String newCategory = newCategories.get(index);
                    UserRoleCategory userRoleCategory = new UserRoleCategory();
                    userRoleCategory.setUserRole(savedUserRole);
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
    public void deleteUserRole(Resource resource, User user, Role role) {
        List<UserRole> userRoles = userRoleRepository.findByResourceAndUser(resource, user);
        if (userRoles.size() <= 1) {
            throw new ApiException(ExceptionCode.IRREMOVABLE_USER_ROLE);
        }

        UserRole userRole = userRoleRepository.findByResourceAndUserAndRole(resource, user, role);
        if(userRole == null) {
            throw new ApiException(ExceptionCode.NONEXISTENT_USER_ROLE);
        }
        userRoleCategoryRepository.deleteByUserRole(userRole);
        userRoleRepository.delete(userRole);
        checkSafety(resource, ExceptionCode.IRREMOVABLE_USER_ROLE);
    }

    private void checkSafety(Resource resource, ExceptionCode exceptionCode) {
        if (resource.getScope() == Scope.DEPARTMENT) {
            List<UserRole> remainingAdminRoles = userRoleRepository.findByResourceAndRole(resource, Role.ADMINISTRATOR);
            if (remainingAdminRoles.isEmpty()) {
                throw new ApiException(exceptionCode);
            }
        }
    }

}
