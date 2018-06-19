package hr.prism.board.mapper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class PostMapperTest {

    @Mock
    private LocationMapper locationMapper;

    @Mock
    private OrganizationMapper organizationMapper;

    @Mock
    private DocumentMapper documentMapper;

    @Mock
    private BoardMapper boardMapper;

    @Mock
    private ResourceMapper resourceMapper;

    @Mock
    private ResourceEventMapper resourceEventMapper;

    private PostMapper postMapper;

    @Before
    public void setUp() {
        postMapper = new PostMapper(
            locationMapper, organizationMapper, documentMapper, boardMapper, resourceMapper, resourceEventMapper);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(
            locationMapper, organizationMapper, documentMapper, boardMapper, resourceMapper, resourceEventMapper);
        reset(locationMapper, organizationMapper, documentMapper, boardMapper, resourceMapper, resourceEventMapper);
    }

    @Test
    public void apply_success() {

    }

    @Test
    public void apply_successWhenNull() {
        assertNull(postMapper.apply(null));
    }

}
