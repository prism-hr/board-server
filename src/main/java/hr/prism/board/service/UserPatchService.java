package hr.prism.board.service;

import hr.prism.board.domain.Document;
import hr.prism.board.domain.User;
import hr.prism.board.dto.DocumentDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class UserPatchService extends PatchService<User> {

    public <U> void patchProperty(User entity, Getter<U> getter, Setter<U> setter, Optional<U> newValueOptional) {
        super.patchProperty(entity, null, getter, setter, newValueOptional);
    }

    public void patchDocument(User entity, Getter<Document> getter, Setter<Document> setter, Optional<DocumentDTO> newValueOptional) {
        super.patchDocument(entity, null, getter, setter, newValueOptional);
    }

}
