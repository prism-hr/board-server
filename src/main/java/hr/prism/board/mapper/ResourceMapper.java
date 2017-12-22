package hr.prism.board.mapper;

import hr.prism.board.domain.Resource;
import hr.prism.board.representation.ResourceRepresentation;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class ResourceMapper {

    public <T extends ResourceRepresentation<T>> T apply(Resource resource, Class<T> representationClass) {
        return applySmall(resource, representationClass)
            .setSummary(resource.getSummary())
            .setState(resource.getState())
            .setCreatedTimestamp(resource.getCreatedTimestamp())
            .setUpdatedTimestamp(resource.getUpdatedTimestamp())
            .setActions(resource.getActions());
    }

    public String getHandle(Resource resource, Resource parent) {
        return resource.getHandle().replaceFirst(parent.getHandle() + "/", "");
    }

    <T extends ResourceRepresentation<T>> T applySmall(Resource resource, Class<T> representationClass) {
        return BeanUtils.instantiate(representationClass)
            .setId(resource.getId())
            .setScope(resource.getScope())
            .setName(resource.getName());
    }

}
