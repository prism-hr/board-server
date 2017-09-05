package hr.prism.board.mapper;

import hr.prism.board.domain.UserRole;
import hr.prism.board.domain.UserRoleCategory;
import hr.prism.board.representation.UserRoleRepresentation;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class UserRoleMapper implements Function<UserRole, UserRoleRepresentation> {

    @Inject
    private UserMapper userMapper;

    @Override
    public UserRoleRepresentation apply(UserRole userRole) {
        if (userRole == null) {
            return null;
        }

        return new UserRoleRepresentation()
            .setUser(userMapper.apply(userRole.getUser()))
            .setRole(userRole.getRole())
            .setState(userRole.getState())
            .setCategories(userRole.getCategories().stream().map(UserRoleCategory::getName).collect(Collectors.toList()))
            .setExpiryDate(userRole.getExpiryDate())
            .setViewed(userRole.isViewed());
    }

}
