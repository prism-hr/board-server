package hr.prism.board.service;

import hr.prism.board.domain.Resource;
import hr.prism.board.dto.LocationDTO;
import hr.prism.board.enums.CategoryType;
import hr.prism.board.exception.ExceptionCode;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
public class ResourcePatchService<T extends Resource> extends PatchService<T> {

    private final ResourceService resourceService;

    @Inject
    public ResourcePatchService(LocationService locationService, DocumentService documentService,
                                ResourceService resourceService) {
        super(locationService, documentService);
        this.resourceService = resourceService;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public void patchName(T resource, Optional<String> newValueOptional, ExceptionCode unique) {
        if (newValueOptional != null) {
            String oldValue = resource.getName();
            if (newValueOptional.isPresent()) {
                String newValue = newValueOptional.get();
                if (!Objects.equals(oldValue, newValue)) {
                    if (unique != null) {
                        resourceService.validateUniqueName(
                            resource.getScope(), resource.getId(), resource.getParent(), newValue, unique);
                    }

                    patchProperty(resource, "name", resource::setName, oldValue, newValue);
                }
            } else if (oldValue != null) {
                patchProperty(resource, "name", resource::setName, oldValue, null);
            }
        }
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public void patchHandle(T resource, Optional<String> newValueOptional, ExceptionCode unique) {
        if (newValueOptional != null) {
            String oldValue = resource.getHandle();
            if (newValueOptional.isPresent()) {
                String newValue = newValueOptional.get();
                Resource parent = resource.getParent();
                if (!Objects.equals(resource, parent)) {
                    newValue = parent.getHandle() + "/" + newValue;
                }

                if (unique != null) {
                    resourceService.validateUniqueHandle(resource, newValue, unique);
                }

                if (!Objects.equals(oldValue, newValue)) {
                    patchHandle(resource, oldValue, newValue);
                }
            } else if (oldValue != null) {
                patchHandle(resource, oldValue, null);
            }
        }
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public void patchLocation(T resource, Optional<LocationDTO> newValueOptional) {
        super.patchLocation(resource, "location",
            resource::getLocation, resource::setLocation, newValueOptional);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public void patchCategories(T resource, CategoryType categoryType,
                                Optional<List<String>> newValuesOptional) {
        if (newValuesOptional != null) {
            List<String> oldValues = resourceService.getCategories(resource, categoryType);
            List<String> newValues = newValuesOptional.orElse(null);
            if (!Objects.equals(oldValues, newValues)) {
                patchCategories(resource, categoryType, oldValues, newValues);
            }
        }
    }

    @Override
    protected <U> void patchProperty(T resource, String property, Setter<U> setter, U oldValue, U newValue) {
        super.patchProperty(resource, property, setter, oldValue, newValue);
        resource.getChangeList().put(property, oldValue, newValue);
    }

    private void patchHandle(T resource, String oldValue, String newValue) {
        resourceService.updateHandle(resource, newValue);
        resource.getChangeList().put("handle", getHandleLeaf(oldValue), getHandleLeaf(newValue));
    }

    private void patchCategories(T resource, CategoryType categoryType, List<String> oldValues,
                                 List<String> newValues) {
        resourceService.updateCategories(resource, categoryType, newValues);
        resource.getChangeList().put(categoryType.name().toLowerCase() + "Categories", oldValues, newValues);
    }

    private String getHandleLeaf(String value) {
        if (value == null) {
            return null;
        }

        String[] parts = value.split("/");
        return parts[(parts.length - 1)];
    }

}
