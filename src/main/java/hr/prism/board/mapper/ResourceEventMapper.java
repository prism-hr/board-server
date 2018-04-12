package hr.prism.board.mapper;

import hr.prism.board.domain.ResourceEvent;
import hr.prism.board.domain.User;
import hr.prism.board.enums.ResourceEventMatch;
import hr.prism.board.representation.ResourceEventRepresentation;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

import static hr.prism.board.enums.ResourceEventMatch.DEFINITE;
import static hr.prism.board.enums.ResourceEventMatch.PROBABLE;
import static java.util.stream.Collectors.toList;

@Component
public class ResourceEventMapper implements Function<ResourceEvent, ResourceEventRepresentation> {

    private final boolean exposeResponseData;

    private final UserMapper userMapper;

    private final DocumentMapper documentMapper;

    private final LocationMapper locationMapper;

    public ResourceEventMapper(@Value("${expose.response.data}") boolean exposeResponseData, UserMapper userMapper,
                               DocumentMapper documentMapper, LocationMapper locationMapper) {
        this.exposeResponseData = exposeResponseData;
        this.userMapper = userMapper;
        this.documentMapper = documentMapper;
        this.locationMapper = locationMapper;
    }

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
                .setReferral(resourceEvent.getReferral())
                .setMatch(getResourceEventMatch(user))
                .setViewed(resourceEvent.isViewed());

        List<ResourceEvent> history = resourceEvent.getHistory();
        if (CollectionUtils.isNotEmpty(history)) {
            representation.setHistory(history.stream().map(this::applyHistory).collect(toList()));
        }

        representation.setGender(resourceEvent.getGender());
        representation.setAgeRange(resourceEvent.getAgeRange());
        representation.setLocationNationality(locationMapper.apply(resourceEvent.getLocationNationality()));
        representation.setMemberCategory(resourceEvent.getMemberCategory());
        representation.setMemberProgram(resourceEvent.getMemberProgram());
        representation.setMemberYear(resourceEvent.getMemberYear());

        if (exposeResponseData || resourceEvent.isExposeResponseData()) {
            representation.setUser(userMapper.apply(resourceEvent.getUser()));
            representation.setIpAddress(resourceEvent.getIpAddress());
            representation.setDocumentResume(documentMapper.apply(resourceEvent.getDocumentResume()));
            representation.setWebsiteResume(resourceEvent.getWebsiteResume());
            representation.setCoveringNote(resourceEvent.getCoveringNote());
        }

        representation.setCreatedTimestamp(resourceEvent.getCreatedTimestamp());
        return representation;
    }

    private ResourceEventRepresentation applyHistory(ResourceEvent resourceEvent) {
        return new ResourceEventRepresentation()
            .setId(resourceEvent.getId())
            .setEvent(resourceEvent.getEvent())
            .setCreatedTimestamp(resourceEvent.getCreatedTimestamp())
            .setMatch(getResourceEventMatch(resourceEvent.getUser()));
    }

    private ResourceEventMatch getResourceEventMatch(User user) {
        return user == null ? PROBABLE : DEFINITE;
    }

}
