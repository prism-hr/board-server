package hr.prism.board.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import hr.prism.board.authentication.AuthenticationToken;
import hr.prism.board.domain.Board;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.Post;
import hr.prism.board.domain.User;
import hr.prism.board.dto.*;
import hr.prism.board.repository.ResourceRepository;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.LocalDateTime;

import static hr.prism.board.enums.ExistingRelation.STUDENT;
import static hr.prism.board.enums.MemberCategory.MASTER_STUDENT;
import static hr.prism.board.enums.MemberCategory.UNDERGRADUATE_STUDENT;
import static java.math.BigDecimal.ONE;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;

@Component
public class DataHelper {

    private final ResourceRepository resourceRepository;

    private final DepartmentService departmentService;

    private final BoardService boardService;

    private final PostService postService;

    private final UserService userService;

    @Inject
    public DataHelper(ResourceRepository resourceRepository, DepartmentService departmentService,
                      BoardService boardService, PostService postService, UserService userService) {
        this.resourceRepository = resourceRepository;
        this.departmentService = departmentService;
        this.boardService = boardService;
        this.postService = postService;
        this.userService = userService;
    }

    Department setUpDepartment(User user, Long universityId, String name) {
        getContext().setAuthentication(new AuthenticationToken(user));
        return departmentService.createDepartment(universityId,
            new DepartmentDTO()
                .setName(name)
                .setSummary(name + " summary"));
    }

    Board setUpBoard(User user, Long departmentId, String name) {
        getContext().setAuthentication(new AuthenticationToken(user));
        return boardService.createBoard(departmentId,
            new BoardDTO()
                .setName(name)
                .setPostCategories(ImmutableList.of("Employment", "Internship")));
    }

    Post setUpPost(User user, Long boardId, String name) {
        getContext().setAuthentication(new AuthenticationToken(user));
        return postService.createPost(boardId,
            new PostDTO()
                .setName(name)
                .setSummary(name + " summary")
                .setOrganization(
                    new OrganizationDTO()
                        .setName("organization"))
                .setLocation(new LocationDTO()
                    .setName("london")
                    .setDomicile("uk")
                    .setGoogleId("google")
                    .setLatitude(ONE)
                    .setLongitude(ONE))
                .setApplyWebsite("http://www.google.co.uk")
                .setPostCategories(ImmutableList.of("Employment", "Internship"))
                .setMemberCategories(ImmutableList.of(UNDERGRADUATE_STUDENT, MASTER_STUDENT))
                .setExistingRelation(STUDENT)
                .setExistingRelationExplanation(ImmutableMap.of("studyLevel", "MASTER"))
                .setLiveTimestamp(LocalDateTime.now())
                .setDeadTimestamp(LocalDateTime.now().plusWeeks(1L)));
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

    User setUpUser(String givenName, String surname, String email) {
        return userService.createUser(
            new RegisterDTO()
                .setGivenName(givenName)
                .setSurname(surname)
                .setEmail(email)
                .setPassword("password"));
    }

}
