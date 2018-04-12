package hr.prism.board.dto;

import hr.prism.board.enums.AgeRange;
import hr.prism.board.enums.DocumentRequestState;
import hr.prism.board.enums.Gender;
import org.hibernate.validator.constraints.Email;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.Optional;

public class UserPatchDTO {

    @Size(min = 1, max = 100)
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<String> givenName;

    @Size(min = 1, max = 100)
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<String> surname;

    @Email
    @Size(max = 255)
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<String> email;

    @Valid
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<DocumentDTO> documentImage;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<DocumentRequestState> documentImageRequestState;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<Boolean> seenWalkThrough;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<Gender> gender;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<AgeRange> ageRange;

    @Valid
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<LocationDTO> locationNationality;

    @Valid
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<DocumentDTO> documentResume;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<String> websiteResume;

    public Optional<String> getGivenName() {
        return givenName;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public UserPatchDTO setGivenName(Optional<String> givenName) {
        this.givenName = givenName;
        return this;
    }

    public Optional<String> getSurname() {
        return surname;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public UserPatchDTO setSurname(Optional<String> surname) {
        this.surname = surname;
        return this;
    }

    public Optional<String> getEmail() {
        return email;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public UserPatchDTO setEmail(Optional<String> email) {
        this.email = email;
        return this;
    }

    public Optional<DocumentDTO> getDocumentImage() {
        return documentImage;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public UserPatchDTO setDocumentImage(Optional<DocumentDTO> documentImage) {
        this.documentImage = documentImage;
        return this;
    }

    public Optional<DocumentRequestState> getDocumentImageRequestState() {
        return documentImageRequestState;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public UserPatchDTO setDocumentImageRequestState(Optional<DocumentRequestState> documentImageRequestState) {
        this.documentImageRequestState = documentImageRequestState;
        return this;
    }

    public Optional<Boolean> getSeenWalkThrough() {
        return seenWalkThrough;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public UserPatchDTO setSeenWalkThrough(Optional<Boolean> seenWalkThrough) {
        this.seenWalkThrough = seenWalkThrough;
        return this;
    }

    public Optional<Gender> getGender() {
        return gender;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public UserPatchDTO setGender(Optional<Gender> gender) {
        this.gender = gender;
        return this;
    }

    public Optional<AgeRange> getAgeRange() {
        return ageRange;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public UserPatchDTO setAgeRange(Optional<AgeRange> ageRange) {
        this.ageRange = ageRange;
        return this;
    }

    public Optional<LocationDTO> getLocationNationality() {
        return locationNationality;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public UserPatchDTO setLocationNationality(Optional<LocationDTO> locationNationality) {
        this.locationNationality = locationNationality;
        return this;
    }

    public Optional<DocumentDTO> getDocumentResume() {
        return documentResume;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public UserPatchDTO setDocumentResume(Optional<DocumentDTO> documentResume) {
        this.documentResume = documentResume;
        return this;
    }

    public Optional<String> getWebsiteResume() {
        return websiteResume;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public UserPatchDTO setWebsiteResume(Optional<String> websiteResume) {
        this.websiteResume = websiteResume;
        return this;
    }

}
