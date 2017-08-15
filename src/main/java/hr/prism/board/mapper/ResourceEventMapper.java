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

        return new ResourceEventRepresentation()
            .setUser(userMapper.apply(resourceEvent.getUser()))
            .setIpAddress(resourceEvent.getIpAddress())
            .setDocumentResume(documentMapper.apply(resourceEvent.getDocumentResume()))
            .setWebsiteResume(resourceEvent.getWebsiteResume());
    }

}
