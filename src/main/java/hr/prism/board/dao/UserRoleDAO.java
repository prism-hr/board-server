package hr.prism.board.dao;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.UserRole;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.State;
import hr.prism.board.repository.UserRepository;
import hr.prism.board.repository.UserRoleRepository;
import hr.prism.board.repository.UserSearchRepository;
import hr.prism.board.value.Statistics;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static hr.prism.board.utils.BoardUtils.makeSoundex;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

@Repository
@Transactional
public class UserRoleDAO {

    private UserRoleRepository userRoleRepository;

    private UserRepository userRepository;

    private UserSearchRepository userSearchRepository;

    private ActivityDAO activityDAO;

    private final EntityManager entityManager;

    @Inject
    public UserRoleDAO(UserRoleRepository userRoleRepository, UserRepository userRepository,
                       UserSearchRepository userSearchRepository, ActivityDAO activityDAO,
                       EntityManager entityManager) {
        this.userRoleRepository = userRoleRepository;
        this.userRepository = userRepository;
        this.userSearchRepository = userSearchRepository;
        this.activityDAO = activityDAO;
        this.entityManager = entityManager;
    }

    public List<UserRole> getUserRoles(Resource resource, List<Role> roles, State state, String searchTerm) {
        List<Long> userIds = userRepository.findByResourceAndRolesAndState(resource, roles, state);
        if (userIds.isEmpty()) {
            return Collections.emptyList();
        }

        String search = UUID.randomUUID().toString();
        boolean searchTermApplied = searchTerm != null;
        if (searchTermApplied) {
            userSearchRepository.insertBySearch(search, LocalDateTime.now(), makeSoundex(searchTerm), userIds);
            entityManager.flush();
        }

        @SuppressWarnings("JpaQlInspection")
        String statement =
            "select distinct userRole " +
                "from UserRole userRole " +
                "inner join userRole.user user " +
                "left join user.searches search on search.search = :search " +
                "where userRole.resource = :resource " +
                "and user.id in (:userIds) " +
                "and userRole.role in(:roles) " +
                "and userRole.state = :state ";
        if (searchTermApplied) {
            statement += "and search.id is not null ";
        }

        statement += "order by search.id, user.id desc";

        List<UserRole> userRoles = entityManager.createQuery(statement, UserRole.class)
            .setParameter("search", search)
            .setParameter("resource", resource)
            .setParameter("userIds", userIds)
            .setParameter("roles", roles)
            .setParameter("state", state)
            .setHint("javax.persistence.fetchgraph",
                entityManager.getEntityGraph("userRole.extended"))
            .getResultList();

        if (searchTermApplied) {
            userSearchRepository.deleteBySearch(search);
        }

        return userRoles;
    }

    public void deleteUserRoles(List<UserRole> userRoles) {
        if (isNotEmpty(userRoles)) {
            activityDAO.deleteActivities(userRoles);
            userRoleRepository.deleteByIds(
                userRoles
                    .stream()
                    .map(UserRole::getId)
                    .collect(toList()));
        }
    }

    @SuppressWarnings("JpaQueryApiInspection")
    public Statistics getMemberStatistics(Long departmentId) {
        return (Statistics) entityManager.createNamedQuery("memberStatistics")
            .setParameter("departmentId", departmentId)
            .getSingleResult();
    }

}
