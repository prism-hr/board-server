package hr.prism.board.service;

import hr.prism.board.repository.ResourceTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.time.LocalDateTime;

@Service
@Transactional
public class ResourceTaskService {

    private static String CREATE_MEMBER = hr.prism.board.enums.ResourceTask.CREATE_MEMBER.name();

    private static String CREATE_INTERNAL_POST = hr.prism.board.enums.ResourceTask.CREATE_INTERNAL_POST.name();

    private static String NOTIFY_AUTHOR = hr.prism.board.enums.ResourceTask.NOTIFY_AUTHOR.name();

    @Inject
    private ResourceTaskRepository resourceTaskRepository;

    public void createForNewResource(Long resourceId) {
        resourceTaskRepository.insertForNewResource(resourceId, CREATE_MEMBER, CREATE_INTERNAL_POST, NOTIFY_AUTHOR, LocalDateTime.now());
    }

}
