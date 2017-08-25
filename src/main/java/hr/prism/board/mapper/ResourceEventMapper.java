package hr.prism.board.mapper;

import hr.prism.board.domain.ResourceEvent;
import hr.prism.board.representation.ResourceEventRepresentation;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.function.Function;

@Service
public class ResourceEventMapper implements Function<ResourceEvent, ResourceEventRepresentation> {

    @Inject
    private DocumentMapper documentMapper;

    @Inject
    private UserMapper userMapper;

    @Override
    public ResourceEventRepresentation apply(ResourceEvent resourceEvent) {
        if (resourceEvent == null) {
            return null;
        }

        ResourceEventRepresentation representation =
            new ResourceEventRepresentation()
                .setEvent(resourceEvent.getEvent())
                .setUser(userMapper.apply(resourceEvent.getUser()))
                .setIpAddress(resourceEvent.getIpAddress())
                .setCreatedTimestamp(resourceEvent.getCreatedTimestamp())
                .setViewed(resourceEvent.isViewed());

        if (resourceEvent.isExposeResponseData()) {
            representation.setDocumentResume(documentMapper.apply(resourceEvent.getDocumentResume()));
            representation.setWebsiteResume(resourceEvent.getWebsiteResume());
            representation.setCoveringNote(resourceEvent.getCoveringNote());
        }

        return representation;
    }

}
