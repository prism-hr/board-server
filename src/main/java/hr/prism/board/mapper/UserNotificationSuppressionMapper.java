package hr.prism.board.mapper;

import hr.prism.board.domain.Resource;
import hr.prism.board.representation.UserNotificationSuppressionRepresentation;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
public class UserNotificationSuppressionMapper {

    @Inject
    private ResourceMapperFactory resourceMapperFactory;

    public UserNotificationSuppressionRepresentation apply(Resource resource, boolean suppressed) {
        return new UserNotificationSuppressionRepresentation()
            .setResource(resourceMapperFactory.applySmall(resource))
            .setSuppressed(suppressed);
    }

}
