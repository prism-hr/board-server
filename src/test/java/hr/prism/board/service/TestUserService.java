package hr.prism.board.service;

import hr.prism.board.authentication.AuthenticationToken;
import hr.prism.board.domain.User;
import hr.prism.board.dto.RegisterDTO;
import hr.prism.board.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;

import static hr.prism.board.TestHelper.FRAMEWORK_TABLES;

@Service
public class TestUserService {

    private final UserRepository userRepository;

    private final AuthenticationService authenticationService;

    private final EntityManager entityManager;

    @Inject
    public TestUserService(UserRepository userRepository, AuthenticationService authenticationService,
                           EntityManager entityManager) {
        this.userRepository = userRepository;
        this.authenticationService = authenticationService;
        this.entityManager = entityManager;
    }

    private SecureRandom random = new SecureRandom();

    public User authenticate() {
        String id = new BigInteger(140, random).toString(30);
        User user = authenticationService.register(
            new RegisterDTO()
                .setGivenName(id)
                .setSurname(id)
                .setEmail(id + "@example.com").setPassword("password"));

        setAuthentication(user);
        return user;
    }

    public void setAuthentication(User user) {
        if (user == null) {
            return;
        }

        AuthenticationToken authentication = new AuthenticationToken(user);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public void unauthenticate() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    public void deleteTestUsers() {
        List<Long> userIds = userRepository.findByTestUser(true);
        if (!userIds.isEmpty()) {
            Query removeForeignKeyChecks = entityManager.createNativeQuery("SET SESSION FOREIGN_KEY_CHECKS = 0");
            removeForeignKeyChecks.executeUpdate();

            @SuppressWarnings("unchecked")
            List<String> tablesNames = entityManager.createNativeQuery("SHOW TABLES").getResultList();

            tablesNames
                .stream()
                .filter(tableName -> !FRAMEWORK_TABLES.contains(tableName))
                .forEach(tableName -> {
                    @SuppressWarnings("SqlResolve")
                    Query deleteUserData = entityManager.createNativeQuery(
                        "DELETE FROM " + tableName + " WHERE creator_id IN (:userIds)");

                    deleteUserData.setParameter("userIds", userIds);
                    deleteUserData.executeUpdate();
                });

            Query restoreForeignKeyChecks = entityManager.createNativeQuery(
                "SET SESSION FOREIGN_KEY_CHECKS = 1");
            restoreForeignKeyChecks.executeUpdate();
        }
    }

}
