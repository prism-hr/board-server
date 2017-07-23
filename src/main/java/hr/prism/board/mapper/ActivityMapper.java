package hr.prism.board.mapper;

import hr.prism.board.domain.Activity;
import hr.prism.board.domain.UserRole;
import hr.prism.board.representation.ActivityRepresentation;
import hr.prism.board.representation.UserRoleRepresentation;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.function.Function;

@Service
public class ActivityMapper implements Function<Activity, ActivityRepresentation> {

    @Inject
    private ResourceMapperFactory resourceMapperFactory;

    @Inject
    private UserRoleMapper userRoleMapper;

    @Inject
    private UserMapper userMapper;

    @Override
    public ActivityRepresentation apply(Activity activity) {
        if (activity == null) {
            return null;
        }

        return new ActivityRepresentation()
            .setId(activity.getId())
            .setResource(resourceMapperFactory.applySmall(activity.getResource()))
            .setUserRole(mapUserRole(activity.getUserRole()))
            .setCategory(activity.getActivity());
    }

    private UserRoleRepresentation mapUserRole(UserRole userRole) {
        if (userRole == null) {
            return null;
        }

        return userRoleMapper.apply(userRole).setUser(userMapper.apply(userRole.getUser()));
    }

}
