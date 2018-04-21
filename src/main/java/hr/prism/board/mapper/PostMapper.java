package hr.prism.board.mapper;

import hr.prism.board.domain.Board;
import hr.prism.board.domain.Post;
import hr.prism.board.representation.PostRepresentation;
import hr.prism.board.service.PostService;
import hr.prism.board.service.ResourceService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.function.Function;

import static hr.prism.board.enums.CategoryType.MEMBER;
import static hr.prism.board.enums.CategoryType.POST;
import static hr.prism.board.enums.MemberCategory.fromStrings;

@Component
public class PostMapper implements Function<Post, PostRepresentation> {

    private final LocationMapper locationMapper;

    private final OrganizationMapper organizationMapper;

    private final DocumentMapper documentMapper;

    private final BoardMapper boardMapper;

    private final ResourceMapper resourceMapper;

    private final ResourceEventMapper resourceEventMapper;

    private final ResourceService resourceService;

    private final PostService postService;

    @Inject
    public PostMapper(LocationMapper locationMapper, OrganizationMapper organizationMapper,
                      DocumentMapper documentMapper, BoardMapper boardMapper, ResourceMapper resourceMapper,
                      ResourceEventMapper resourceEventMapper, ResourceService resourceService,
                      PostService postService) {
        this.locationMapper = locationMapper;
        this.organizationMapper = organizationMapper;
        this.documentMapper = documentMapper;
        this.boardMapper = boardMapper;
        this.resourceMapper = resourceMapper;
        this.resourceEventMapper = resourceEventMapper;
        this.resourceService = resourceService;
        this.postService = postService;
    }

    @Override
    public PostRepresentation apply(Post post) {
        if (post == null) {
            return null;
        }

        PostRepresentation representation =
            resourceMapper.apply(post, PostRepresentation.class)
                .setSummary(post.getSummary())
                .setDescription(post.getDescription())
                .setOrganization(organizationMapper.apply(post.getOrganization()))
                .setLocation(locationMapper.apply(post.getLocation()))
                .setExistingRelation(post.getExistingRelation())
                .setExistingRelationExplanation(
                    postService.mapExistingRelationExplanation(post.getExistingRelationExplanation()))
                .setPostCategories(resourceService.getCategories(post, POST))
                .setMemberCategories(fromStrings(resourceService.getCategories(post, MEMBER)));

        String applyEmail = post.getApplyEmail();
        if (post.isExposeApplyData()) {
            representation.setApplyWebsite(post.getApplyWebsite());
            representation.setApplyDocument(documentMapper.apply(post.getApplyDocument()));
            representation.setApplyEmail(applyEmail);
        } else if (applyEmail != null) {
            representation.setApplyEmail(post.getApplyEmailDisplay());
        }

        return representation.setBoard(boardMapper.applySmall((Board) post.getParent()))
            .setLiveTimestamp(post.getLiveTimestamp())
            .setDeadTimestamp(post.getDeadTimestamp())
            .setViewCount(post.getViewCount())
            .setReferralCount(post.getReferralCount())
            .setResponseCount(post.getResponseCount())
            .setLastViewTimestamp(post.getLastViewTimestamp())
            .setLastReferralTimestamp(post.getLastReferralTimestamp())
            .setLastResponseTimestamp(post.getLastResponseTimestamp())
            .setResponseReadiness(post.getDemographicDataStatus())
            .setReferral(resourceEventMapper.apply(post.getReferral()))
            .setResponse(resourceEventMapper.apply(post.getResponse()));
    }

    PostRepresentation applySmall(Post post) {
        if (post == null) {
            return null;
        }

        return resourceMapper.applySmall(post, PostRepresentation.class)
            .setOrganization(organizationMapper.apply(post.getOrganization()))
            .setLocation(locationMapper.apply(post.getLocation()))
            .setBoard(boardMapper.applySmall((Board) post.getParent()));
    }

}
