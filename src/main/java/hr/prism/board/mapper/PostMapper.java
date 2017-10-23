package hr.prism.board.mapper;

import hr.prism.board.domain.Board;
import hr.prism.board.domain.Post;
import hr.prism.board.enums.CategoryType;
import hr.prism.board.enums.MemberCategory;
import hr.prism.board.representation.PostRepresentation;
import hr.prism.board.service.PostService;
import hr.prism.board.service.ResourceService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.function.Function;

@Service
public class PostMapper implements Function<Post, PostRepresentation> {

    @Inject
    private LocationMapper locationMapper;

    @Inject
    private DocumentMapper documentMapper;

    @Inject
    private BoardMapper boardMapper;

    @Inject
    private ResourceMapper resourceMapper;

    @Inject
    private ResourceEventMapper resourceEventMapper;

    @Inject
    private ResourceService resourceService;

    @Inject
    private PostService postService;

    @Override
    public PostRepresentation apply(Post post) {
        if (post == null) {
            return null;
        }

        PostRepresentation representation =
            resourceMapper.apply(post, PostRepresentation.class)
                .setDescription(post.getDescription())
                .setInternal(post.getInternal())
                .setOrganizationName(post.getOrganizationName())
                .setLocation(locationMapper.apply(post.getLocation()))
                .setExistingRelation(post.getExistingRelation())
                .setExistingRelationExplanation(postService.mapExistingRelationExplanation(post.getExistingRelationExplanation()))
                .setPostCategories(resourceService.getCategories(post, CategoryType.POST))
                .setMemberCategories(MemberCategory.fromStrings(resourceService.getCategories(post, CategoryType.MEMBER)));

        String applyEmail = post.getApplyEmail();
        if (post.isExposeApplyData()) {
            representation.setApplyWebsite(post.getApplyWebsite());
            representation.setApplyDocument(documentMapper.apply(post.getApplyDocument()));
            representation.setApplyEmail(applyEmail);
        } else if (applyEmail != null) {
            representation.setApplyEmail(post.getApplyEmailDisplay());
        }

        return representation.setBoard(boardMapper.apply((Board) post.getParent()))
            .setLiveTimestamp(post.getLiveTimestamp())
            .setDeadTimestamp(post.getDeadTimestamp())
            .setViewCount(post.getViewCount())
            .setReferralCount(post.getReferralCount())
            .setResponseCount(post.getResponseCount())
            .setLastViewTimestamp(post.getLastViewTimestamp())
            .setLastReferralTimestamp(post.getLastReferralTimestamp())
            .setLastResponseTimestamp(post.getLastResponseTimestamp())
            .setResponseReadiness(post.getResponseReadiness())
            .setReferral(resourceEventMapper.apply(post.getReferral()))
            .setResponse(resourceEventMapper.apply(post.getResponse()));
    }

    public PostRepresentation applySmall(Post post) {
        if (post == null) {
            return null;
        }

        return resourceMapper.applySmall(post, PostRepresentation.class)
            .setOrganizationName(post.getOrganizationName())
            .setLocation(locationMapper.apply(post.getLocation()))
            .setBoard(boardMapper.applySmall((Board) post.getParent()));
    }

}
