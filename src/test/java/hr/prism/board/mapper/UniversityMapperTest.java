package hr.prism.board.mapper;

import hr.prism.board.domain.Document;
import hr.prism.board.domain.University;
import hr.prism.board.representation.DocumentRepresentation;
import hr.prism.board.representation.UniversityRepresentation;
import hr.prism.board.value.ResourceSearch;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UniversityMapperTest {

    @Mock
    private ResourceMapper resourceMapper;

    @Mock
    private DocumentMapper documentMapper;

    private UniversityMapper universityMapper;

    private DocumentRepresentation documentLogoRepresentation;

    @Before
    public void setUp() {
        documentLogoRepresentation = new DocumentRepresentation().setCloudinaryId("cloudinaryId");
        universityMapper = new UniversityMapper(resourceMapper, documentMapper);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(resourceMapper, documentMapper);
        reset(resourceMapper, documentMapper);
    }

    @Test
    public void apply_success() {
        Document documentLogo = new Document();
        documentLogo.setCloudinaryId("cloudinaryId");

        University university = new University();
        university.setId(1L);
        university.setHomepage("homepage");
        university.setDocumentLogo(documentLogo);
        university.setHandle("handle");

        when(resourceMapper.applySmall(university, UniversityRepresentation.class))
            .thenReturn(new UniversityRepresentation());

        when(documentMapper.apply(documentLogo)).thenReturn(documentLogoRepresentation);

        UniversityRepresentation universityRepresentation = universityMapper.apply(university);

        assertEquals("homepage", universityRepresentation.getHomepage());
        assertEquals(documentLogoRepresentation, universityRepresentation.getDocumentLogo());
        assertEquals("handle", universityRepresentation.getHandle());

        verify(resourceMapper, times(1)).applySmall(university, UniversityRepresentation.class);
        verify(documentMapper, times(1)).apply(documentLogo);
    }

    @Test
    public void apply_successWhenNull() {
        assertNull(universityMapper.apply((University) null));
    }

    @Test
    public void apply_resourceSearch_success() {
        ResourceSearch resourceSearch = new ResourceSearch(1L, "university",
            "cloudinaryId", "cloudinaryUrl", "fileName");

        when(documentMapper.apply(resourceSearch)).thenReturn(documentLogoRepresentation);

        UniversityRepresentation universityRepresentation = universityMapper.apply(resourceSearch);

        assertEquals(1L, universityRepresentation.getId().longValue());
        assertEquals("university", universityRepresentation.getName());
        assertEquals(documentLogoRepresentation, universityRepresentation.getDocumentLogo());

        verify(documentMapper, times(1)).apply(resourceSearch);
    }

    @Test
    public void apply_resourceSearch_successWhenNull() {
        assertNull(universityMapper.apply((ResourceSearch) null));
    }

}
