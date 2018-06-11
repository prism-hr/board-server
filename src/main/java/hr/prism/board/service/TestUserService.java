package hr.prism.board.service;

import hr.prism.board.dao.TestUserDAO;
import hr.prism.board.repository.UserRepository;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

@Service
@Transactional
public class TestUserService {

    private static final Logger LOGGER = getLogger(TestUserService.class);

    private final UserRepository userRepository;

    private final TestUserDAO testUserDAO;

    public TestUserService(UserRepository userRepository, TestUserDAO testUserDAO) {
        this.userRepository = userRepository;
        this.testUserDAO = testUserDAO;
    }

    public void deleteTestUsers() {
        List<Long> userIds = userRepository.findByTestUser(true);
        if (userIds.isEmpty()) {
            return;
        }

        testUserDAO.disableForeignKeyChecks();
        testUserDAO.getTablesWithCreatorIdColumn()
            .forEach(tableName -> {
                LOGGER.info("Deleting test users from table: " + tableName);
                testUserDAO.deleteRecords(tableName, userIds);
            });

        testUserDAO.enableForeignKeyChecks();
    }

}
