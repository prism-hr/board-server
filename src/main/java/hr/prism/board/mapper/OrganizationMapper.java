package hr.prism.board.mapper;

import hr.prism.board.domain.Organization;
import hr.prism.board.representation.OrganizationRepresentation;
import hr.prism.board.representation.OrganizationStatisticsRepresentation;
import hr.prism.board.value.OrganizationSearch;
import hr.prism.board.value.OrganizationStatistics;
import org.springframework.stereotype.Component;

import java.util.function.Function;

import static org.springframework.beans.BeanUtils.instantiate;

@Component
public class OrganizationMapper implements Function<Organization, OrganizationRepresentation> {

    @Override
    public OrganizationRepresentation apply(Organization organization) {
        if (organization == null) {
            return null;
        }

        return new OrganizationRepresentation()
            .setId(organization.getId())
            .setName(organization.getName())
            .setLogo(organization.getLogo());
    }

    public OrganizationRepresentation apply(OrganizationSearch organization) {
        if (organization == null) {
            return null;
        }

        //noinspection unchecked
        return applySmall(organization, OrganizationRepresentation.class);
    }

    public OrganizationStatisticsRepresentation apply(OrganizationStatistics organization) {
        if (organization == null) {
            return null;
        }

        return applySmall(organization, OrganizationStatisticsRepresentation.class)
            .setPostCount(organization.getPostCount())
            .setMostRecentPost(organization.getMostRecentPost())
            .setPostViewCount(organization.getPostViewCount())
            .setPostReferralCount(organization.getPostReferralCount())
            .setPostResponseCount(organization.getPostResponseCount());
    }

    private <T extends OrganizationRepresentation<T>> T applySmall(OrganizationSearch organization,
                                                                   Class<T> representationClass) {
        return instantiate(representationClass)
            .setId(organization.getId())
            .setName(organization.getName())
            .setLogo(organization.getLogo());
    }

}
