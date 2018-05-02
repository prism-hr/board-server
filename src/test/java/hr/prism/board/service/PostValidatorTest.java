package hr.prism.board.service;

import com.google.common.collect.ImmutableList;
import hr.prism.board.domain.Document;
import hr.prism.board.domain.Post;
import hr.prism.board.enums.State;
import hr.prism.board.exception.BoardException;
import hr.prism.board.validation.PostValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.stream.Stream;

import static hr.prism.board.enums.ExistingRelation.STUDENT;
import static hr.prism.board.enums.State.DRAFT;
import static hr.prism.board.exception.ExceptionCode.*;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;

@RunWith(MockitoJUnitRunner.class)
public class PostValidatorTest {

    private PostValidator postValidator;

    @Before
    public void setUp() {
        postValidator = spy(new PostValidator());
        doNothing().when(postValidator).checkApplyWebsite(any(Post.class));
    }

    @Test
    public void checkApply_successWhenWebsite() {
        Post post = new Post();
        post.setApplyWebsite("website");
        postValidator.checkApply(post);
    }

    @Test
    public void checkApply_successWhenDocument() {
        Post post = new Post();
        post.setApplyDocument(new Document());
        postValidator.checkApply(post);
    }

    @Test
    public void checkApply_successWhenEmail() {
        Post post = new Post();
        post.setApplyEmail("email@prism.hr");
        postValidator.checkApply(post);
    }

    @Test
    public void checkApply_failureWhenNone() {
        assertThatThrownBy(() -> postValidator.checkApply(new Post()))
            .isExactlyInstanceOf(BoardException.class)
            .hasFieldOrPropertyWithValue("exceptionCode", MISSING_APPLY_OPTION);
    }

    @Test
    public void checkApply_failureWhenTwoOrMore() {
        Post post0 = new Post();
        post0.setApplyWebsite("website");
        post0.setApplyDocument(new Document());

        Post post1 = new Post();
        post1.setApplyWebsite("website");
        post1.setApplyEmail("email@prism.hr");

        Post post2 = new Post();
        post2.setApplyDocument(new Document());
        post2.setApplyEmail("email@prism.hr");

        Post post3 = new Post();
        post3.setApplyWebsite("website");
        post3.setApplyDocument(new Document());
        post3.setApplyEmail("email@prism.hr");

        Stream.of(post0, post1, post2, post3).forEach(post ->
            assertThatThrownBy(() -> postValidator.checkApply(post))
                .isExactlyInstanceOf(BoardException.class)
                .hasFieldOrPropertyWithValue("exceptionCode", CORRUPTED_APPLY_OPTION));
    }

    @Test
    public void checkCategories_successWhenForbiddenAndEmpty() {
        postValidator.checkCategories(emptyList(), emptyList(),
            FORBIDDEN_POST_CATEGORIES, MISSING_POST_CATEGORIES, INVALID_POST_CATEGORIES);
    }

    @Test
    public void checkCategories_successWhenRequiredAndProvided() {
        List<String> referenceCategories = ImmutableList.of("category1", "category2");

        postValidator.checkCategories(singletonList("category1"), referenceCategories,
            FORBIDDEN_POST_CATEGORIES, MISSING_POST_CATEGORIES, INVALID_POST_CATEGORIES);

        postValidator.checkCategories(referenceCategories, referenceCategories,
            FORBIDDEN_POST_CATEGORIES, MISSING_POST_CATEGORIES, INVALID_POST_CATEGORIES);
    }

    @Test
    public void checkCategories_failureWhenForbiddenAndProvided() {
        assertThatThrownBy(() -> postValidator.checkCategories(singletonList("category1"), emptyList(),
            FORBIDDEN_POST_CATEGORIES, MISSING_POST_CATEGORIES, INVALID_POST_CATEGORIES))
            .isExactlyInstanceOf(BoardException.class)
            .hasFieldOrPropertyWithValue("exceptionCode", FORBIDDEN_POST_CATEGORIES);
    }

    @Test
    public void checkCategories_failureWhenRequiredAndEmpty() {
        assertThatThrownBy(() -> postValidator.checkCategories(emptyList(), singletonList("category1"),
            FORBIDDEN_POST_CATEGORIES, MISSING_POST_CATEGORIES, INVALID_POST_CATEGORIES))
            .isExactlyInstanceOf(BoardException.class)
            .hasFieldOrPropertyWithValue("exceptionCode", MISSING_POST_CATEGORIES);
    }

    @Test
    public void checkCategories_failureWhenRequiredProvidedAndNonCompliant() {
        List<String> referenceCategories = ImmutableList.of("category1", "category2");

        assertThatThrownBy(() -> postValidator.checkCategories(singletonList("category3"), referenceCategories,
            FORBIDDEN_POST_CATEGORIES, MISSING_POST_CATEGORIES, INVALID_POST_CATEGORIES))
            .isExactlyInstanceOf(BoardException.class)
            .hasFieldOrPropertyWithValue("exceptionCode", INVALID_POST_CATEGORIES);

        assertThatThrownBy(() -> postValidator.checkCategories(ImmutableList.of("category1", "category3"),
            referenceCategories, FORBIDDEN_POST_CATEGORIES, MISSING_POST_CATEGORIES, INVALID_POST_CATEGORIES))
            .isExactlyInstanceOf(BoardException.class)
            .hasFieldOrPropertyWithValue("exceptionCode", INVALID_POST_CATEGORIES);

        assertThatThrownBy(() -> postValidator.checkCategories(ImmutableList.of("category1", "category2", "category3"),
            referenceCategories, FORBIDDEN_POST_CATEGORIES, MISSING_POST_CATEGORIES, INVALID_POST_CATEGORIES))
            .isExactlyInstanceOf(BoardException.class)
            .hasFieldOrPropertyWithValue("exceptionCode", INVALID_POST_CATEGORIES);

        assertThatThrownBy(() -> postValidator.checkCategories(ImmutableList.of("category3", "category4"),
            referenceCategories, FORBIDDEN_POST_CATEGORIES, MISSING_POST_CATEGORIES, INVALID_POST_CATEGORIES))
            .isExactlyInstanceOf(BoardException.class)
            .hasFieldOrPropertyWithValue("exceptionCode", INVALID_POST_CATEGORIES);
    }

    @Test
    public void checkExistingRelation_successWhenNotDraft() {
        Stream.of(State.values())
            .filter(state -> DRAFT != state)
            .forEach(state -> {
                Post post = new Post();
                post.setState(state);
                postValidator.checkExistingRelation(post);
            });
    }

    @Test
    public void checkExistingRelation_successWhenDraftWithRelation() {
        Post post = new Post();
        post.setState(DRAFT);
        post.setExistingRelation(STUDENT);
        postValidator.checkExistingRelation(post);
    }

    @Test
    public void checkExistingRelation_failureWhenDraftWithoutRelation() {
        Post post = new Post();
        post.setState(DRAFT);

        assertThatThrownBy(() -> postValidator.checkExistingRelation(post))
            .isExactlyInstanceOf(BoardException.class)
            .hasFieldOrPropertyWithValue("exceptionCode", MISSING_EXISTING_RELATION);
    }

}
