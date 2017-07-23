package hr.prism.board.mapper;

import hr.prism.board.domain.Resource;
import hr.prism.board.representation.ResourceRepresentation;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class ResourceMapper {

    @Inject
    private DocumentMapper documentMapper;

    @SuppressWarnings("unchecked")
    // FIXME: move logo mapping into this class
    public <T extends ResourceRepresentation> T apply(Resource resource, Class<T> representationClass) {
        return (T) BeanUtils.instantiate(representationClass)
            .setId(resource.getId())
            .setScope(resource.getScope())
            .setName(resource.getName())
            .setSummary(resource.getSummary())
            .setDocumentLogo(documentMapper.apply(resource.getDocumentLogo()))
            .setState(resource.getState())
            .setCreatedTimestamp(resource.getCreatedTimestamp())
            .setUpdatedTimestamp(resource.getUpdatedTimestamp())
            .setActions(resource.getActions());
    }

    public ResourceRepresentation mapParentResource(Resource resource) {
        Resource parent = resource.getParent();
        if (parent.equals(resource)) {
            return null;
        }

        return apply(parent, ResourceRepresentation.class);
    }

}
