package hr.prism.board.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

@Service
public class MigrationService {

    @Value("migration.on")
    private boolean migrationOn;

    @Inject
    private DepartmentService departmentService;

    @Inject
    private BoardService boardService;

    @Inject
    private PostService postService;

    @Inject
    private UserService userService;

    @Async
    @PostConstruct
    public void postConstruct() {
        if (migrationOn) {
            departmentService.findAllIds().forEach(id -> departmentService.migrate(id));
            boardService.findAllIds().forEach(id -> boardService.migrate(id));
            postService.findAllIds().forEach(id -> postService.migrate(id));
            userService.findAllIds().forEach(id -> userService.migrate(id));
        }
    }

}
