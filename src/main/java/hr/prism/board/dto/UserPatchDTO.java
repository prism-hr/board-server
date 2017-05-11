package hr.prism.board.dto;

import javax.validation.Valid;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class UserPatchDTO {

    private Optional<String> givenName;

    private Optional<String> surname;

    @Valid
    private Optional<DocumentDTO> documentImage;

    public Optional<String> getGivenName() {
        return givenName;
    }

    public UserPatchDTO setGivenName(Optional<String> givenName) {
        this.givenName = givenName;
        return this;
    }

    public Optional<String> getSurname() {
        return surname;
    }

    public UserPatchDTO setSurname(Optional<String> surname) {
        this.surname = surname;
        return this;
    }

    public Optional<DocumentDTO> getDocumentImage() {
        return documentImage;
    }

    public UserPatchDTO setDocumentImage(Optional<DocumentDTO> documentImage) {
        this.documentImage = documentImage;
        return this;
    }
}
