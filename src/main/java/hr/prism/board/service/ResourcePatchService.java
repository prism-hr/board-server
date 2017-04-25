package hr.prism.board.service;

import hr.prism.board.domain.Document;
import hr.prism.board.domain.Location;
import hr.prism.board.domain.Resource;
import hr.prism.board.dto.DocumentDTO;
import hr.prism.board.dto.LocationDTO;
import hr.prism.board.enums.CategoryType;
import hr.prism.board.exception.ApiException;
import hr.prism.board.exception.ExceptionCode;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.*;

@Service
@Transactional
public class ResourcePatchService {
    
    @Inject
    private DocumentService documentService;
    
    @Inject
    private LocationService locationService;
    
    @Inject
    private ResourceService resourceService;
    
    public void patchName(Resource resource, Optional<String> newValueOptional, ExceptionCode required, ExceptionCode unique) {
        if (newValueOptional != null) {
            String oldValue = resource.getName();
            if (newValueOptional.isPresent()) {
                String newValue = newValueOptional.get();
                if (!Objects.equals(oldValue, newValue)) {
                    if (unique != null) {
                        resourceService.validateUniqueName(resource.getScope(), resource.getId(), resource.getParent(), newValue, unique);
                    }
                    
                    patchProperty(resource, "name", resource::setName, oldValue, newValue);
                }
            } else if (required != null) {
                throw new ApiException(required);
            } else if (oldValue != null) {
                patchProperty(resource, "name", resource::setName, oldValue, null);
            }
        }
    }
    
    public void patchHandle(Resource resource, Optional<String> newValueOptional, ExceptionCode required, ExceptionCode unique) {
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
                
                patchHandle(resource, oldValue, newValue);
            } else if (required != null) {
                throw new ApiException(required);
            } else if (oldValue != null) {
                patchHandle(resource, oldValue, null);
            }
        }
    }
    
    public <T> void patchProperty(Resource resource, String property, Getter<T> getter, Setter<T> setter, Optional<T> newValueOptional) {
        patchProperty(resource, property, getter, setter, newValueOptional, null, null);
    }
    
    public <T> void patchProperty(Resource resource, String property, Getter<T> getter, Setter<T> setter, Optional<T> newValueOptional, ExceptionCode required) {
        patchProperty(resource, property, getter, setter, newValueOptional, required, null);
    }
    
    public <T> void patchProperty(Resource resource, String property, Getter<T> getter, Setter<T> setter, Optional<T> newValueOptional, Runnable after) {
        patchProperty(resource, property, getter, setter, newValueOptional, null, after);
    }
    
    public void patchDocument(Resource resource, String property, Getter<Document> getter, Setter<Document> setter, Optional<DocumentDTO> newValueOptional) {
        patchDocument(resource, property, getter, setter, newValueOptional, null);
    }
    
    public void patchDocument(Resource resource, String property, Getter<Document> getter, Setter<Document> setter, Optional<DocumentDTO> newValueOptional, Runnable after) {
        if (newValueOptional != null) {
            Document oldValue = getter.get();
            if (newValueOptional.isPresent()) {
                DocumentDTO newValue = newValueOptional.get();
                if (!Objects.equals(oldValue.getCloudinaryId(), newValue.getCloudinaryId())) {
                    patchDocument(resource, property, setter, oldValue, newValue);
                }
                
                if (after != null) {
                    after.run();
                }
            } else if (oldValue != null) {
                patchDocument(resource, property, setter, oldValue, null);
            }
        }
    }
    
    public void patchLocation(Resource resource, Optional<LocationDTO> newValueOptional, ExceptionCode required) {
        if (newValueOptional != null) {
            Location oldValue = resource.getLocation();
            if (newValueOptional.isPresent()) {
                LocationDTO newValue = newValueOptional.get();
                if (!Objects.equals(oldValue.getGoogleId(), newValue.getGoogleId())) {
                    patchLocation(resource, oldValue, newValue);
                }
            } else if (required != null) {
                throw new ApiException(required);
            } else if (oldValue != null) {
                patchLocation(resource, oldValue, null);
            }
        }
    }
    
    public void patchCategories(Resource resource, CategoryType categoryType, Optional<List<String>> newValuesOptional) {
        if (newValuesOptional != null) {
            List<String> oldValues = resourceService.getCategories(resource, categoryType);
            if (newValuesOptional.isPresent()) {
                List<String> newValues = new ArrayList<>(newValuesOptional.get());
                if (!Objects.equals(oldValues, newValues)) {
                    newValues.sort(Comparator.naturalOrder());
                    patchCategories(resource, categoryType, oldValues, newValues);
                }
            } else if (oldValues != null) {
                patchCategories(resource, categoryType, oldValues, null);
            }
        }
    }
    
    private <T> void patchProperty(Resource resource, String property, Getter<T> getter, Setter<T> setter, Optional<T> newValueOptional, ExceptionCode required, Runnable after) {
        if (newValueOptional != null) {
            T oldValue = getter.get();
            if (newValueOptional.isPresent()) {
                T newValue = newValueOptional.get();
                if (!Objects.equals(oldValue, newValue)) {
                    patchProperty(resource, property, setter, oldValue, newValue);
                }
                
                if (after != null) {
                    after.run();
                }
            } else if (required != null) {
                throw new ApiException(required);
            } else if (oldValue != null) {
                patchProperty(resource, property, setter, oldValue, null);
            }
        }
    }
    
    private <T> void patchProperty(Resource resource, String property, Setter<T> setter, T oldValue, T newValue) {
        setter.set(newValue);
        resource.getChangeList().put(property, oldValue, newValue);
    }
    
    private void patchHandle(Resource resource, String oldValue, String newValue) {
        resourceService.updateHandle(resource, newValue);
        resource.getChangeList().put("handle", oldValue, newValue);
    }
    
    private void patchDocument(Resource resource, String property, Setter<Document> setter, Document oldValue, DocumentDTO newValue) {
        patchProperty(resource, property, setter, oldValue, documentService.getOrCreateDocument(newValue));
        documentService.deleteDocument(oldValue);
    }
    
    private void patchLocation(Resource resource, Location oldValue, LocationDTO newValue) {
        patchProperty(resource, "location", resource::setLocation, oldValue, locationService.getOrCreateLocation(newValue));
        locationService.deleteLocation(oldValue);
    }
    
    private void patchCategories(Resource resource, CategoryType categoryType, List<String> oldValues, List<String> newValues) {
        resourceService.updateCategories(resource, categoryType, newValues);
        resource.getChangeList().put(categoryType.name().toLowerCase() + "Categories", oldValues, newValues);
    }
    
    public interface Getter<T> {
        T get();
    }
    
    public interface Setter<T> {
        void set(T value);
    }
    
}