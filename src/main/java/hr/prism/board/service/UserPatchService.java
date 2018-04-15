package hr.prism.board.service;

import hr.prism.board.domain.Document;
import hr.prism.board.domain.Location;
import hr.prism.board.domain.User;
import hr.prism.board.dto.DocumentDTO;
import hr.prism.board.dto.LocationDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Optional;

@Service
@Transactional
public class UserPatchService extends PatchService<User> {

    @Inject
    public UserPatchService(LocationService locationService, DocumentService documentService) {
        super(locationService, documentService);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public <U> void patchProperty(User entity, Getter<U> getter, Setter<U> setter, Optional<U> newValueOptional) {
        super.patchProperty(entity, null, getter, setter, newValueOptional);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public void patchDocument(User entity, Getter<Document> getter, Setter<Document> setter,
                              Optional<DocumentDTO> newValueOptional) {
        super.patchDocument(entity, null, getter, setter, newValueOptional);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public void patchLocation(User entity, Getter<Location> getter, Setter<Location> setter,
                              Optional<LocationDTO> newValueOptional) {
        super.patchLocation(entity, null, getter, setter, newValueOptional);
    }

}
