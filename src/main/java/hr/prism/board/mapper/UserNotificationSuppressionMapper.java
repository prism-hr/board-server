package hr.prism.board.mapper;

import hr.prism.board.domain.Resource;
import hr.prism.board.representation.ResourceRepresentation;
import hr.prism.board.representation.UserNotificationSuppressionRepresentation;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class UserNotificationSuppressionMapper {

    @Inject
    private ResourceMapper resourceMapper;

    public UserNotificationSuppressionRepresentation apply(Resource resource, boolean suppressed) {
        return new UserNotificationSuppressionRepresentation()
            .setResource(resourceMapper.apply(resource, ResourceRepresentation.class))
            .setParentResource(resourceMapper.mapParentResource(resource))
            .setSuppressed(suppressed);
    }

}
