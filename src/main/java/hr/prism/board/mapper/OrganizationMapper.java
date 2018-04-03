package hr.prism.board.mapper;

import hr.prism.board.representation.OrganizationRepresentation;
import hr.prism.board.value.Organization;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class OrganizationMapper implements Function<Organization, OrganizationRepresentation> {

    @Override
    public OrganizationRepresentation apply(Organization organization) {
        if (organization == null) {
            return null;
        }

        return applySmall(organization)
            .setPostCount(organization.getPostCount())
            .setMostRecentPost(organization.getMostRecentPost())
            .setPostViewCount(organization.getPostViewCount())
            .setPostReferralCount(organization.getPostReferralCount())
            .setPostResponseCount(organization.getPostResponseCount());
    }

    public OrganizationRepresentation applySmall(Organization organization) {
        if (organization == null) {
            return null;
        }

        return new OrganizationRepresentation()
            .setName(organization.getName())
            .setLogo(organization.getLogo());
    }

}
