package hr.prism.board.service;

import hr.prism.board.dao.PostResponseDAO;
import hr.prism.board.domain.*;
import hr.prism.board.dto.ResourceEventDTO;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.value.DemographicDataStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

import static hr.prism.board.enums.Action.EDIT;
import static hr.prism.board.enums.Action.PURSUE;
import static hr.prism.board.enums.Scope.POST;
import static hr.prism.board.enums.State.ACCEPTED;
import static hr.prism.board.exception.ExceptionCode.FORBIDDEN_REFERRAL;
import static hr.prism.board.exception.ExceptionCode.INVALID_REFERRAL;

@Service
@Transactional
public class PostResponseService {

    private final PostResponseDAO postResponseDAO;

    private final ResourceService resourceService;

    private final UserService userService;

    private final ActionService actionService;

    private final ResourceEventService resourceEventService;

    private final ActivityService activityService;

    private final DepartmentUserService departmentUserService;

    private final EntityManager entityManager;

    @Inject
    public PostResponseService(PostResponseDAO postResponseDAO, ResourceService resourceService,
                               UserService userService, ActionService actionService,
                               ResourceEventService resourceEventService, ActivityService activityService,
                               DepartmentUserService departmentUserService, EntityManager entityManager) {
        this.postResponseDAO = postResponseDAO;
        this.resourceService = resourceService;
        this.userService = userService;
        this.actionService = actionService;
        this.resourceEventService = resourceEventService;
        this.activityService = activityService;
        this.departmentUserService = departmentUserService;
        this.entityManager = entityManager;
    }


    public ResourceEvent getPostResponse(Long postId, Long responseId) {
        return getPostResponse(userService.getUserSecured(), postId, responseId);
    }

    public List<ResourceEvent> getPostResponses(Long id, String searchTerm) {
        User user = userService.getUserSecured();
        Post post = (Post) resourceService.getResource(user, POST, id);
        actionService.executeAction(user, post, EDIT, () -> post);
        return postResponseDAO.getPostResponses(user, post, searchTerm);
    }

    public String consumePostReferral(String referral) {
        ResourceEvent resourceEvent = resourceEventService.getAndConsumeReferral(referral);
        Post post = (Post) resourceEvent.getResource();
        checkValidDemographicData(resourceEvent.getUser(), (Department) post.getParent().getParent());

        Document applyDocument = post.getApplyDocument();
        String redirect = applyDocument == null ? post.getApplyWebsite() : applyDocument.getCloudinaryUrl();
        if (post.getState() != ACCEPTED || redirect == null) {
            // We may no longer be redirecting - throw an exception so client can refresh
            throw new BoardException(INVALID_REFERRAL, "Post no longer accepting referrals");
        }

        return redirect;
    }

    public ResourceEvent createPostResponse(Long id, ResourceEventDTO resourceEvent) {
        User user = userService.getUserSecured();
        Post post = (Post) resourceService.getResource(user, POST, id);
        actionService.executeAction(user, post, PURSUE, () -> {
            checkValidDemographicData(user, (Department) post.getParent().getParent());
            return post;
        });

        actionService.executeAction(user, post, PURSUE, () -> post);
        return resourceEventService.getOrCreatePostResponse(post, user, resourceEvent)
            .setExposeResponseData(true);
    }

    public ResourceEvent createPostResponseView(Long postId, Long responseId) {
        User user = userService.getUserSecured();
        ResourceEvent resourceEvent = getPostResponse(user, postId, responseId);
        activityService.viewActivity(resourceEvent.getActivity(), user);
        return resourceEvent.setViewed(true);
    }

    private ResourceEvent getPostResponse(User user, Long id, Long responseId) {
        Post post = (Post) resourceService.getResource(user, POST, id);
        actionService.executeAction(user, post, EDIT, () -> post);
        ResourceEvent resourceEvent = resourceEventService.getById(responseId);
        resourceEvent.setExposeResponseData(resourceEvent.getUser().equals(user));
        return resourceEvent;
    }

    private void checkValidDemographicData(User user, Department department) {
        entityManager.flush();
        DemographicDataStatus dataStatus = departmentUserService.makeDemographicDataStatus(user, department);
        if (dataStatus.isRequireUserData()) {
            throw new BoardForbiddenException(FORBIDDEN_REFERRAL, "User data not valid / complete");
        }

        if (dataStatus.isRequireMemberData()) {
            throw new BoardForbiddenException(FORBIDDEN_REFERRAL, "Member data not valid / complete");
        }
    }

}
