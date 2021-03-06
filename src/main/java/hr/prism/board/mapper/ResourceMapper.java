package hr.prism.board.mapper;

import hr.prism.board.domain.Resource;
import hr.prism.board.representation.ResourceRepresentation;
import org.springframework.stereotype.Component;

import static org.springframework.beans.BeanUtils.instantiate;

@Component
public class ResourceMapper {

    public <T extends ResourceRepresentation<T>> T apply(Resource resource, Class<T> representationClass) {
        return applySmall(resource, representationClass)
            .setState(resource.getState())
            .setCreatedTimestamp(resource.getCreatedTimestamp())
            .setUpdatedTimestamp(resource.getUpdatedTimestamp())
            .setActions(resource.getActions());
    }

    <T extends ResourceRepresentation<T>> T applySmall(Resource resource, Class<T> representationClass) {
        return instantiate(representationClass)
            .setId(resource.getId())
            .setScope(resource.getScope())
            .setName(resource.getName());
    }

    String getHandle(Resource resource, Resource parent) {
        return resource.getHandle().replaceFirst(parent.getHandle() + "/", "");
    }

}
