package hr.prism.board.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import hr.prism.board.domain.ResourceOperation;
import hr.prism.board.representation.ResourceChangeListRepresentation;
import hr.prism.board.representation.ResourceOperationRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.util.function.Function;

@Service
public class ResourceOperationMapper implements Function<ResourceOperation, ResourceOperationRepresentation> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceOperationMapper.class);

    @Inject
    private UserMapper userMapper;

    @Inject
    private ObjectMapper objectMapper;

    @Override
    public ResourceOperationRepresentation apply(ResourceOperation resourceOperation) {
        if (resourceOperation == null) {
            return null;
        }

        String changeList = resourceOperation.getChangeList();
        ResourceChangeListRepresentation changeListRepresentation = null;
        if (changeList != null) {
            try {
                changeListRepresentation = objectMapper.readValue(changeList, ResourceChangeListRepresentation.class);
            } catch (IOException e) {
                LOGGER.info("Could not deserialize change list", e);
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
