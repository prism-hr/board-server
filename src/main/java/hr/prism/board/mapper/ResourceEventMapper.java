package hr.prism.board.mapper;

import hr.prism.board.domain.ResourceEvent;
import hr.prism.board.domain.User;
import hr.prism.board.enums.ResourceEventMatch;
import hr.prism.board.representation.ResourceEventRepresentation;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ResourceEventMapper implements Function<ResourceEvent, ResourceEventRepresentation> {

    @Inject
    private UserMapper userMapper;

    @Inject
    private DocumentMapper documentMapper;

    @Inject
    private LocationMapper locationMapper;

    @Override
    public ResourceEventRepresentation apply(ResourceEvent resourceEvent) {
        if (resourceEvent == null) {
            return null;
        }

        User user = resourceEvent.getUser();
        ResourceEventRepresentation representation =
            new ResourceEventRepresentation()
                .setId(resourceEvent.getId())
                .setEvent(resourceEvent.getEvent())
                .setUser(userMapper.apply(resourceEvent.getUser()))
                .setIpAddress(resourceEvent.getIpAddress())
                .setReferral(resourceEvent.getReferral())
                .setMatch(getResourceEventMatch(user))
                .setViewed(resourceEvent.isViewed());

        List<ResourceEvent> history = resourceEvent.getHistory();
        if (CollectionUtils.isNotEmpty(history)) {
            representation.setHistory(history.stream().map(this::applyHistory).collect(Collectors.toList()));
        }

        representation.setGender(resourceEvent.getGender());
        representation.setAgeRange(resourceEvent.getAgeRange());
        representation.setLocationNationality(locationMapper.apply(resourceEvent.getLocationNationality()));
        representation.setMemberCategory(resourceEvent.getMemberCategory());
        representation.setMemberProgram(resourceEvent.getMemberProgram());
        representation.setMemberYear(resourceEvent.getMemberYear());

        if (resourceEvent.isExposeResponseData()) {
            representation.setDocumentResume(documentMapper.apply(resourceEvent.getDocumentResume()));
            representation.setWebsiteResume(resourceEvent.getWebsiteResume());
            representation.setCoveringNote(resourceEvent.getCoveringNote());
        }

        representation.setCreatedTimestamp(resourceEvent.getCreatedTimestamp());
        return representation;
    }

    private ResourceEventRepresentation applyHistory(ResourceEvent resourceEvent) {
        return new ResourceEventRepresentation()
            .setEvent(resourceEvent.getEvent())
            .setCreatedTimestamp(resourceEvent.getCreatedTimestamp())
            .setMatch(getResourceEventMatch(resourceEvent.getUser()));
    }

    private ResourceEventMatch getResourceEventMatch(User user) {
        return user == null ? ResourceEventMatch.PROBABLE : ResourceEventMatch.DEFINITE;
    }

}
