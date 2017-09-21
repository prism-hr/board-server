package hr.prism.board.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

@Service
public class MigrationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MigrationService.class);

    @Value("${migration.on}")
    private boolean migrationOn;

    private volatile boolean pending = true;

    @Inject
    private DepartmentService departmentService;

    @Inject
    private BoardService boardService;

    @Inject
    private PostService postService;

    @Inject
    private UserService userService;

    @Async
    @EventListener
    public void migrate(ContextRefreshedEvent event) {
        if (migrationOn) {
            if (pending) {
                synchronized (this) {
                    if (pending) {
                        migrate("departments", departmentService.findAllIds(), departmentService::migrate);
                        migrate("boards", boardService.findAllIds(), boardService::migrate);
                        migrate("posts", postService.findAllIds(), postService::migrate);
                        migrate("users", userService.findAllIds(), userService::migrate);
                        pending = false;
                    }
                }
            }
        }
    }

    private void migrate(String entity, List<Long> ids, Migrator migrator) {
        int migrations = ids.size();
        LOGGER.info("Started migrating " + migrations + " " + entity);
        ids.forEach(migrator::migrate);
        LOGGER.info("Finished migrating " + migrations + " " + entity);
    }

    private interface Migrator {
        void migrate(Long id);
    }

}
