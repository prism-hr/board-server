package hr.prism.board.service;

import hr.prism.board.domain.BoardEntity;
import hr.prism.board.domain.Document;
import hr.prism.board.dto.DocumentDTO;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Objects;
import java.util.Optional;

@Service
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public abstract class PatchService<T extends BoardEntity> {

    @Inject
    private DocumentService documentService;

    public <U> void patchProperty(T entity, String property, Getter<U> getter, Setter<U> setter, Optional<U> newValueOptional) {
        if (newValueOptional != null) {
            U oldValue = getter.get();
            if (newValueOptional.isPresent()) {
                U newValue = newValueOptional.get();
                if (!Objects.equals(oldValue, newValue)) {
                    patchProperty(entity, property, setter, oldValue, newValue);
                }
            } else if (oldValue != null) {
                patchProperty(entity, property, setter, oldValue, null);
            }
        }
    }

    public void patchDocument(T entity, String property, Getter<Document> getter, Setter<Document> setter, Optional<DocumentDTO> newValueOptional) {
        if (newValueOptional != null) {
            Document oldValue = getter.get();
            if (newValueOptional.isPresent()) {
                DocumentDTO newValue = newValueOptional.get();
                if (oldValue == null || !Objects.equals(oldValue.getId(), newValue.getId())) {
                    patchDocument(entity, property, setter, oldValue, newValue);
                }
            } else if (oldValue != null) {
                patchDocument(entity, property, setter, oldValue, null);
            }
        }
    }

    protected <U> void patchProperty(T entity, String property, Setter<U> setter, U oldValue, U newValue) {
        setter.set(newValue);
    }

    private void patchDocument(T entity, String property, Setter<Document> setter, Document oldValue, DocumentDTO newValue) {
        patchProperty(entity, property, setter, oldValue, newValue == null ? null : documentService.getOrCreateDocument(newValue));
        if (oldValue != null) {
            documentService.deleteDocument(oldValue);
        }
    }

    public interface Getter<U> {
        U get();
    }

    public interface Setter<U> {
        void set(U value);
    }

}
