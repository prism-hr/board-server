package hr.prism.board.repository;

import hr.prism.board.DBTestContext;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.ResourceCategory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;

import static hr.prism.board.enums.CategoryType.MEMBER;
import static hr.prism.board.enums.CategoryType.POST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@DBTestContext
@RunWith(SpringRunner.class)
@Sql("classpath:data/resourceCategoryRepository_setUp.sql")
@Sql(value = "classpath:data/resourceCategoryRepository_tearDown.sql", executionPhase = AFTER_TEST_METHOD)
public class ResourceCategoryRepositoryIT {

    @Inject
    private ResourceRepository resourceRepository;

    @Inject
    private ResourceCategoryRepository resourceCategoryRepository;

    @Test
    public void deleteByResourceAndType_success() {
        Resource resource = resourceRepository.findOne(1L);

        ResourceCategory category1 = new ResourceCategory();
        category1.setResource(resource);
        category1.setType(MEMBER);
        category1.setName("category1");

        ResourceCategory category2 = new ResourceCategory();
        category2.setResource(resource);
        category2.setType(MEMBER);
        category2.setName("category2");

        ResourceCategory category3 = new ResourceCategory();
        category3.setResource(resource);
        category3.setType(POST);
        category3.setName("category3");

        assertThat(resourceCategoryRepository.findAll()).containsExactlyInAnyOrder(category1, category2, category3);

        resourceCategoryRepository.deleteByResourceAndType(resource, MEMBER);
        assertThat(resourceCategoryRepository.findAll()).containsExactly(category3);
    }

}
