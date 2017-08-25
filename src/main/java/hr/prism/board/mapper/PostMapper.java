package hr.prism.board.mapper;

import hr.prism.board.domain.Board;
import hr.prism.board.domain.Post;
import hr.prism.board.enums.CategoryType;
import hr.prism.board.enums.MemberCategory;
import hr.prism.board.representation.PostApplyRepresentation;
import hr.prism.board.representation.PostRepresentation;
import hr.prism.board.service.PostService;
import hr.prism.board.service.ResourceService;
import hr.prism.board.util.BoardUtils;
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
                .setOrganizationName(post.getOrganizationName())
                .setLocation(locationMapper.apply(post.getLocation()))
                .setExistingRelation(post.getExistingRelation())
                .setExistingRelationExplanation(postService.mapExistingRelationExplanation(post.getExistingRelationExplanation()))
                .setPostCategories(resourceService.getCategories(post, CategoryType.POST))
                .setMemberCategories(MemberCategory.fromStrings(resourceService.getCategories(post, CategoryType.MEMBER)));

        if (post.isExposeApplyData()) {
            representation.setApplyWebsite(post.getApplyWebsite());
            representation.setApplyDocument(documentMapper.apply(post.getApplyDocument()));
            representation.setApplyEmail(post.getApplyEmail());
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
            .setResponded(post.isResponded());
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

    public PostApplyRepresentation applyPostApply(Post post) {
        String applyEmail = post.getApplyEmail();
        applyEmail = applyEmail == null ? null : BoardUtils.obfuscateEmail(applyEmail);

        return new PostApplyRepresentation()
            .setApplyDocument(documentMapper.apply(post.getApplyDocument()))
            .setApplyWebsite(post.getApplyWebsite())
            .setApplyEmail(applyEmail);
    }

}
