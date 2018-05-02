package hr.prism.board.service;

import hr.prism.board.DbTestContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;

@DbTestContext
@RunWith(SpringRunner.class)
public class PostServiceIT {

    @Inject
    private PostService postService;

    @Inject
    private ServiceHelper serviceHelper;

    @Test
    public void createPost_successWhenUnprivileged() {

    }

    @Test
    public void createPost_successWhenDepartmentAuthor() {

    }

    @Test
    public void createPost_failureWhenMissingApply() {

    }

    @Test
    public void createPost_failureWhenCorruptedApply() {

    }

    @Test
    public void createPost_failureWhenApplyWebsiteNotUrl() {

    }

    @Test
    public void createPost_failureWhenMissingPostCategories() {

    }

    @Test
    public void createPost_failureWhenForbiddenPostCategories() {

    }

    @Test
    public void createPost_failureWhenInvalidPostCategories() {

    }

    @Test
    public void createPost_failureWhenMissingMemberCategories() {

    }

    @Test
    public void createPost_failureWhenForbiddenMemberCategories() {

    }

    @Test
    public void createPost_failureWhenInvalidMemberCategories() {

    }

    @Test
    public void createPost_failureWhenUnprivilegedAndMissingRelationExplanation() {

    }

}
