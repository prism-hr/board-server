package hr.prism.board.mapper;

import hr.prism.board.domain.Resource;
import hr.prism.board.representation.UserNotificationSuppressionRepresentation;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.function.Function;

@Component
public class UserNotificationSuppressionMapper
    implements Function<Resource, UserNotificationSuppressionRepresentation> {

    private final ResourceMapperFactory resourceMapperFactory;

    @Inject
    public UserNotificationSuppressionMapper(ResourceMapperFactory resourceMapperFactory) {
        this.resourceMapperFactory = resourceMapperFactory;
    }

    @Override
    public UserNotificationSuppressionRepresentation apply(Resource resource) {
        if (resource == null) {
            return null;
        }

        return new UserNotificationSuppressionRepresentation()
            .setResource(resourceMapperFactory.applySmall(resource))
            .setSuppressed(resource.isNotificationSuppressedForUser());
    }

}
