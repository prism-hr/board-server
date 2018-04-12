package hr.prism.board.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import hr.prism.board.domain.ResourceOperation;
import hr.prism.board.representation.ChangeListRepresentation;
import hr.prism.board.representation.ResourceOperationRepresentation;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;
import java.util.function.Function;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class ResourceOperationMapper implements Function<ResourceOperation, ResourceOperationRepresentation> {

    private static final Logger LOGGER = getLogger(ResourceOperationMapper.class);

    private final UserMapper userMapper;

    private final ObjectMapper objectMapper;

    @Inject
    public ResourceOperationMapper(UserMapper userMapper, ObjectMapper objectMapper) {
        this.userMapper = userMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public ResourceOperationRepresentation apply(ResourceOperation resourceOperation) {
        if (resourceOperation == null) {
            return null;
        }

        String changeList = resourceOperation.getChangeList();
        ChangeListRepresentation changeListRepresentation = null;
        if (changeList != null) {
            try {
                changeListRepresentation = objectMapper.readValue(changeList, ChangeListRepresentation.class);
            } catch (IOException e) {
                LOGGER.warn("Could not deserialize change list", e);
            }
        }

        return new ResourceOperationRepresentation()
            .setAction(resourceOperation.getAction())
            .setUser(userMapper.apply(resourceOperation.getUser()))
            .setChangeList(changeListRepresentation)
            .setComment(resourceOperation.getComment())
            .setCreatedTimestamp(resourceOperation.getCreatedTimestamp());
    }

}
