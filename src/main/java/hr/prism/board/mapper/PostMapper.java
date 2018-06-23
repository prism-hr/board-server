package hr.prism.board.mapper;

import hr.prism.board.domain.Board;
import hr.prism.board.domain.Post;
import hr.prism.board.representation.DemographicDataStatusRepresentation;
import hr.prism.board.representation.PostRepresentation;
import hr.prism.board.value.DemographicDataStatus;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.function.Function;

import static hr.prism.board.enums.MemberCategory.fromStrings;

@Component
public class PostMapper implements Function<Post, PostRepresentation> {

    private final LocationMapper locationMapper;

    private final OrganizationMapper organizationMapper;

    private final DocumentMapper documentMapper;

    private final BoardMapper boardMapper;

    private final ResourceMapper resourceMapper;

    private final ResourceEventMapper resourceEventMapper;

    @Inject
    public PostMapper(LocationMapper locationMapper, OrganizationMapper organizationMapper,
                      DocumentMapper documentMapper, BoardMapper boardMapper, ResourceMapper resourceMapper,
                      ResourceEventMapper resourceEventMapper) {
        this.locationMapper = locationMapper;
        this.organizationMapper = organizationMapper;
        this.documentMapper = documentMapper;
        this.boardMapper = boardMapper;
        this.resourceMapper = resourceMapper;
        this.resourceEventMapper = resourceEventMapper;
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
                .setExistingRelationExplanation(post.getExistingRelationExplanation())
                .setPostCategories(post.getPostCategoryStrings())
                .setMemberCategories(fromStrings(post.getMemberCategoryStrings()));

        String applyEmail = post.getApplyEmail();
        if (post.isExposeApplyData()) {
            representation.setApplyWebsite(post.getApplyWebsite());
            representation.setApplyDocument(documentMapper.apply(post.getApplyDocument()));
            representation.setApplyEmail(applyEmail);
        } else if (applyEmail != null) {
            representation.setApplyEmail(post.getApplyEmailDisplay());
        }

        return representation
            .setBoard(boardMapper.applyMedium((Board) post.getParent()))
            .setLiveTimestamp(post.getLiveTimestamp())
            .setDeadTimestamp(post.getDeadTimestamp())
            .setViewCount(post.getViewCount())
            .setReferralCount(post.getReferralCount())
            .setResponseCount(post.getResponseCount())
            .setLastViewTimestamp(post.getLastViewTimestamp())
            .setLastReferralTimestamp(post.getLastReferralTimestamp())
            .setLastResponseTimestamp(post.getLastResponseTimestamp())
            .setResponseReadiness(applyDemographicDataStatus(post.getDemographicDataStatus()))
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

    private DemographicDataStatusRepresentation applyDemographicDataStatus(
        DemographicDataStatus demographicDataStatus) {
        if (demographicDataStatus == null) {
            return null;
        }

        return new DemographicDataStatusRepresentation()
            .setRequireUserData(demographicDataStatus.isRequireUserData())
            .setRequireMemberData(demographicDataStatus.isRequireMemberData())
            .setMemberCategory(demographicDataStatus.getMemberCategory())
            .setMemberProgram(demographicDataStatus.getMemberProgram())
            .setMemberYear(demographicDataStatus.getMemberYear())
            .setExpiryDate(demographicDataStatus.getExpiryDate());
    }

}
