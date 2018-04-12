package hr.prism.board.mapper;

import hr.prism.board.domain.*;
import hr.prism.board.enums.Scope;
import hr.prism.board.representation.ActivityRepresentation;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class ActivityMapper implements Function<Activity, ActivityRepresentation> {

    @Override
    public ActivityRepresentation apply(Activity activity) {
        if (activity == null) {
            return null;
        }

        ActivityRepresentation representation =
            new ActivityRepresentation().setId(activity.getId()).setActivity(activity.getActivity());

        Resource resource = activity.getResource();
        representation.setResourceId(resource.getId());
        representation.setHandle(resource.getHandle());

        Scope scope = resource.getScope();
        switch (scope) {
            case DEPARTMENT:
                representation.setImage(getDocumentLogo((Department) resource));
                representation.setDepartment(resource.getName());
                break;
            case BOARD:
                representation.setImage(getDocumentLogo((Department) resource.getParent()));
                representation.setDepartment(resource.getParent().getName());
                representation.setBoard(resource.getName());
                break;
            case POST:
                Resource parent = resource.getParent();
                representation.setImage(getDocumentLogo((Department) parent.getParent()));
                representation.setDepartment(parent.getParent().getName());
                representation.setBoard(parent.getName());
                representation.setPost(resource.getName());
                break;
            default:
                throw new UnsupportedOperationException("Not expecting resource of scope: " + scope);
        }

        UserRole userRole = activity.getUserRole();
        if (userRole != null) {
            User user = userRole.getUser();
            representation.setUserId(user.getId());
            representation.setRole(userRole.getRole());

            Document documentImage = user.getDocumentImage();
            representation.setImage(getDocumentCloudinaryId(documentImage));
            representation.setGivenName(user.getGivenName());
            representation.setSurname(user.getSurname());
        }

        ResourceEvent resourceEvent = activity.getResourceEvent();
        if (resourceEvent != null) {
            representation.setUserId(resourceEvent.getUser().getId());
            representation.setEvent(resourceEvent.getEvent());

            representation.setGender(resourceEvent.getGender());
            representation.setAgeRange(resourceEvent.getAgeRange());
            representation.setLocation(resourceEvent.getLocationNationality().getName());
        }

        return representation.setViewed(activity.isViewed()).setCreated(activity.getCreatedTimestamp());
    }

    private String getDocumentLogo(Department department) {
        Document documentLogo = department.getDocumentLogo();
        return getDocumentCloudinaryId(documentLogo);
    }

    private String getDocumentCloudinaryId(Document documentLogo) {
        return documentLogo == null ? null : documentLogo.getCloudinaryId();
    }

}
