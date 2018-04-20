package hr.prism.board.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import hr.prism.board.enums.RoleType;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.EXISTING_PROPERTY;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;

@JsonTypeInfo(use = NAME, include = EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = StaffDTO.class, name = "STAFF"),
    @JsonSubTypes.Type(value = MemberDTO.class, name = "MEMBER")})
public abstract class UserRoleDTO<T extends UserRoleDTO> {

    @NotNull
    private RoleType type;

    @Valid
    @NotNull
    private UserDTO user;

    public UserRoleDTO(RoleType type) {
        this.type = type;
    }

    public RoleType getType() {
        return type;
    }

    public UserDTO getUser() {
        return user;
    }

    @SuppressWarnings("unchecked")
    public T setUser(UserDTO user) {
        this.user = user;
        return (T) this;
    }

}
