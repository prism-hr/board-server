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
import static hr.prism.board.enums.ResourceEvent.REFERRAL;
import static hr.prism.board.enums.ResourceEvent.RESPONSE;
import static hr.prism.board.enums.Scope.POST;
import static hr.prism.board.enums.State.ACCEPTED;
import static hr.prism.board.exception.ExceptionCode.FORBIDDEN_REFERRAL;
import static hr.prism.board.exception.ExceptionCode.INVALID_REFERRAL;

@Service
@Transactional
public class PostResponseService {

    private final PostResponseDAO postResponseDAO;

    private final ResourceService resourceService;

    private final ActionService actionService;

    private final ResourceEventService resourceEventService;

    private final ActivityService activityService;

    private final DepartmentUserService departmentUserService;

    private final EntityManager entityManager;

    @Inject
    public PostResponseService(PostResponseDAO postResponseDAO, ResourceService resourceService,
                               ActionService actionService, ResourceEventService resourceEventService,
                               ActivityService activityService, DepartmentUserService departmentUserService,
                               EntityManager entityManager) {
        this.postResponseDAO = postResponseDAO;
        this.resourceService = resourceService;
        this.actionService = actionService;
        this.resourceEventService = resourceEventService;
        this.activityService = activityService;
        this.departmentUserService = departmentUserService;
        this.entityManager = entityManager;
    }

    public ResourceEvent getPostResponse(User user, Long id, Long responseId) {
        Post post = (Post) resourceService.getResource(user, POST, id);
        actionService.executeAction(user, post, EDIT, () -> post);
        ResourceEvent resourceEvent = resourceEventService.getById(responseId);
        resourceEvent.setExposeResponseData(resourceEvent.getUser().equals(user));
        return resourceEvent;
    }

    public List<ResourceEvent> getPostResponses(User user, Long id, String searchTerm) {
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

    public ResourceEvent createPostResponse(User user, Long id, ResourceEventDTO resourceEvent) {
        Post post = (Post) resourceService.getResource(user, POST, id);
        actionService.executeAction(user, post, PURSUE, () -> post);
        checkValidDemographicData(user, (Department) post.getParent().getParent());

        return resourceEventService.createPostResponse(post, user, resourceEvent)
            .setExposeResponseData(true);
    }

    public ResourceEvent createPostResponseView(User user, Long postId, Long responseId) {
        ResourceEvent resourceEvent = getPostResponse(user, postId, responseId);
        activityService.viewActivity(resourceEvent.getActivity(), user);
        return resourceEvent.setViewed(true);
    }

    public void addPostResponseReadiness(Post post, User user) {
        if (user != null && actionService.canExecuteAction(post, PURSUE)) {
            DemographicDataStatus responseReadiness =
                departmentUserService.makeDemographicDataStatus(user, (Department) post.getParent().getParent());
            post.setDemographicDataStatus(responseReadiness);
            if (responseReadiness.isReady() && post.getApplyEmail() == null) {
                resourceEventService.createPostReferral(post, user);
            }
        }
    }

    public void addPostResponse(Post post, User user) {
        if (user != null) {
            entityManager.flush();
            post.setExposeApplyData(actionService.canExecuteAction(post, EDIT));
            post.setReferral(resourceEventService.getResourceEvent(post, REFERRAL, user));
            post.setResponse(resourceEventService.getResourceEvent(post, RESPONSE, user));
        }
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
