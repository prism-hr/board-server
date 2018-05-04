package hr.prism.board.service;

import hr.prism.board.domain.Organization;
import hr.prism.board.domain.Post;
import hr.prism.board.dto.LocationDTO;
import hr.prism.board.dto.OrganizationDTO;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
public class PostPatchService extends ResourcePatchService<Post> {

    private OrganizationService organizationService;

    @Inject
    public PostPatchService(LocationService locationService, DocumentService documentService,
                            ResourceService resourceService, OrganizationService organizationService) {
        super(locationService, documentService, resourceService);
        this.organizationService = organizationService;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public void patchOrganization(Post post, Optional<OrganizationDTO> newValueOptional) {
        patchOrganization(post, "organization",
            post::getOrganization, post::setOrganization, newValueOptional);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    void patchLocation(Post post, Optional<LocationDTO> newValueOptional) {
        super.patchLocation(post, "location",
            post::getLocation, post::setLocation, newValueOptional);
    }

    @SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "SameParameterValue"})
    private void patchOrganization(Post entity, String property, Getter<Organization> getter,
                                   Setter<Organization> setter, Optional<OrganizationDTO> newValueOptional) {
        if (newValueOptional != null) {
            Organization oldValue = getter.get();
            if (newValueOptional.isPresent()) {
                OrganizationDTO newValue = newValueOptional.get();
                if (oldValue == null || !Objects.equals(oldValue.getName(), newValue.getName())) {
                    patchOrganization(entity, property, setter, oldValue, newValue);
                }
            } else if (oldValue != null) {
                patchOrganization(entity, property, setter, oldValue, null);
            }
        }
    }

    private void patchOrganization(Post entity, String property, Setter<Organization> setter, Organization oldValue,
                                   OrganizationDTO newValue) {
        patchProperty(entity, property, setter, oldValue,
            newValue == null ? null : organizationService.getOrCreateOrganization(newValue));
    }

}
