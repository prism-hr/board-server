package hr.prism.board.mapper;

import hr.prism.board.domain.ResourceEvent;
import hr.prism.board.representation.ResourceEventRepresentation;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class ResourceEventMapper implements Function<ResourceEvent, ResourceEventRepresentation> {

    private final UserMapper userMapper;

    private final DocumentMapper documentMapper;

    private final LocationMapper locationMapper;

    public ResourceEventMapper(UserMapper userMapper, DocumentMapper documentMapper, LocationMapper locationMapper) {
        this.userMapper = userMapper;
        this.documentMapper = documentMapper;
        this.locationMapper = locationMapper;
    }

    @Override
    public ResourceEventRepresentation apply(ResourceEvent resourceEvent) {
        if (resourceEvent == null) {
            return null;
        }

        ResourceEventRepresentation representation =
            new ResourceEventRepresentation()
                .setId(resourceEvent.getId())
                .setEvent(resourceEvent.getEvent())
                .setReferral(resourceEvent.getReferral())
                .setViewed(resourceEvent.isViewed())
                .setGender(resourceEvent.getGender())
                .setAgeRange(resourceEvent.getAgeRange())
                .setLocationNationality(locationMapper.apply(resourceEvent.getLocationNationality()))
                .setMemberCategory(resourceEvent.getMemberCategory())
                .setMemberProgram(resourceEvent.getMemberProgram())
                .setMemberYear(resourceEvent.getMemberYear());

        if (resourceEvent.isExposeResponseData()) {
            representation.setUser(userMapper.apply(resourceEvent.getUser()));
            representation.setIpAddress(resourceEvent.getIpAddress());
            representation.setDocumentResume(documentMapper.apply(resourceEvent.getDocumentResume()));
            representation.setWebsiteResume(resourceEvent.getWebsiteResume());
            representation.setCoveringNote(resourceEvent.getCoveringNote());
        }

        return representation
            .setCreatedTimestamp(resourceEvent.getCreatedTimestamp());
    }

}
