package hr.prism.board.dto;

import hr.prism.board.enums.DocumentRequestState;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class UserPatchDTO {
    
    @NotEmpty
    @Size(max = 100)
    private Optional<String> givenName;
    
    @NotEmpty
    @Size(max = 100)
    private Optional<String> surname;
    
    @Valid
    private Optional<DocumentDTO> documentImage;
    
    @NotNull
    private Optional<DocumentRequestState> documentImageRequestState;
    
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
    
    public Optional<DocumentRequestState> getDocumentImageRequestState() {
        return documentImageRequestState;
    }
    
    public UserPatchDTO setDocumentImageRequestState(Optional<DocumentRequestState> documentImageRequestState) {
        this.documentImageRequestState = documentImageRequestState;
        return this;
    }
    
}
