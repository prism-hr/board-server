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
    public void createPost_successWhenApplyWebsite() {

    }

    @Test
    public void createPost_successWhenApplyDocument() {

    }

    @Test
    public void createPost_successWhenApplyEmail() {

    }

}
