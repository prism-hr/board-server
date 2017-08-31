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

    @Inject
    private ResourceEventMapper resourceEventMapper;

    @Override
    public ActivityRepresentation apply(Activity activity) {
        if (activity == null) {
            return null;
        }

        return new ActivityRepresentation()
            .setId(activity.getId())
            .setResource(resourceMapperFactory.applySmall(activity.getResource()))
            .setUserRole(mapUserRole(activity.getUserRole()))
            .setResourceEvent(resourceEventMapper.apply(activity.getResourceEvent()))
            .setActivity(activity.getActivity());
    }

    private UserRoleRepresentation mapUserRole(UserRole userRole) {
        UserRoleRepresentation userRoleRepresentation = userRoleMapper.apply(userRole);
        return userRoleRepresentation == null ? null : userRoleRepresentation.setUser(userMapper.apply(userRole.getUser()));
    }

}
