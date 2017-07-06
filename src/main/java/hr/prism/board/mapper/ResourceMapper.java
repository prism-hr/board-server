package hr.prism.board.mapper;

import hr.prism.board.domain.Resource;
import hr.prism.board.representation.ResourceRepresentation;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class ResourceMapper {

    @SuppressWarnings("unchecked")
    public <T extends ResourceRepresentation> T apply(Resource resource, Class<T> representationClass) {
        return (T) BeanUtils.instantiate(representationClass)
            .setId(resource.getId())
            .setScope(resource.getScope())
            .setName(resource.getName())
            .setSummary(resource.getSummary())
            .setState(resource.getState())
            .setCreatedTimestamp(resource.getCreatedTimestamp())
            .setUpdatedTimestamp(resource.getUpdatedTimestamp())
            .setActions(resource.getActions())
            .setCreated(resource.getCreated());
    }

}
