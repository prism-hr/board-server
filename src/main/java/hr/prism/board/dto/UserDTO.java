package hr.prism.board.dto;

import hr.prism.board.enums.AgeRange;
import hr.prism.board.enums.Gender;
import org.hibernate.validator.constraints.Email;

import javax.validation.Valid;
import javax.validation.constraints.Size;

public class UserDTO {

    private Long id;

    @Size(min = 1, max = 100)
    private String givenName;

    @Size(min = 1, max = 100)
    private String surname;

    @Email
    private String email;

    private Gender gender;

    private AgeRange ageRange;

    @Valid
    private LocationDTO locationNationality;

    public Long getId() {
        return id;
    }

    public UserDTO setId(Long id) {
        this.id = id;
        return this;
    }

    public String getGivenName() {
        return givenName;
    }

    public UserDTO setGivenName(String givenName) {
        this.givenName = givenName;
        return this;
    }

    public String getSurname() {
        return surname;
    }

    public UserDTO setSurname(String surname) {
        this.surname = surname;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public UserDTO setEmail(String email) {
        this.email = email;
        return this;
    }

    public Gender getGender() {
        return gender;
    }

    public UserDTO setGender(Gender gender) {
        this.gender = gender;
        return this;
    }

    public AgeRange getAgeRange() {
        return ageRange;
    }

    public UserDTO setAgeRange(AgeRange ageRange) {
        this.ageRange = ageRange;
        return this;
    }

    public LocationDTO getLocationNationality() {
        return locationNationality;
    }

    public UserDTO setLocationNationality(LocationDTO locationNationality) {
        this.locationNationality = locationNationality;
        return this;
    }

    @Override
    public String toString() {
        return givenName + " " + surname + " (" + email + ")";
    }

}
