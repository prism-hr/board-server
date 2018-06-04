package hr.prism.board.service;

import hr.prism.board.domain.BoardEntity;
import hr.prism.board.domain.Post;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.enums.Action;
import hr.prism.board.repository.ResourceRepository;
import hr.prism.board.representation.ActionRepresentation;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Component
public class ServiceHelper {

    private final ResourceRepository resourceRepository;

    @Inject
    public ServiceHelper(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    void setPostPending(Post post) {
        post.setLiveTimestamp(LocalDateTime.now());
        post.setDeadTimestamp(LocalDateTime.now().plusWeeks(1L));
        resourceRepository.save(post);
    }

    void setPostExpired(Post post) {
        post.setLiveTimestamp(LocalDateTime.now().minusWeeks(1));
        post.setDeadTimestamp(LocalDateTime.now());
        resourceRepository.save(post);
    }

    void verifyIdentity(Resource resource, Resource expectedParentResource, String expectedName) {
        assertNotNull(resource.getId());
        assertEquals(expectedParentResource, resource.getParent());
        assertEquals(expectedName, resource.getName());
    }

    void verifyActions(Resource resource, Action[] expectedActions) {
        assertThat(
            resource.getActions()
                .stream()
                .map(ActionRepresentation::getAction)
                .collect(toList()))
            .containsExactly(expectedActions);
    }

    void verifyTimestamps(BoardEntity entity, LocalDateTime baseline) {
        assertThat(entity.getCreatedTimestamp()).isGreaterThanOrEqualTo(baseline);
        assertThat(entity.getUpdatedTimestamp()).isGreaterThanOrEqualTo(baseline);
    }

    String getUserGivenName(User user) {
        return Optional.ofNullable(user)
            .map(User::getGivenName)
            .orElse("anonymous");
    }

}
