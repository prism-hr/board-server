package hr.prism.board.mapper;

import hr.prism.board.domain.Resource;
import hr.prism.board.representation.UserNotificationSuppressionRepresentation;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class UserNotificationSuppressionMapper {

    private final ResourceMapperFactory resourceMapperFactory;

    @Inject
    public UserNotificationSuppressionMapper(ResourceMapperFactory resourceMapperFactory) {
        this.resourceMapperFactory = resourceMapperFactory;
    }

    public UserNotificationSuppressionRepresentation apply(Resource resource, boolean suppressed) {
        return new UserNotificationSuppressionRepresentation()
            .setResource(resourceMapperFactory.applySmall(resource))
            .setSuppressed(suppressed);
    }

}
