package hr.prism.board.mapper;

import com.google.common.collect.ImmutableSet;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.Document;
import hr.prism.board.domain.ResourceCategory;
import hr.prism.board.domain.University;
import hr.prism.board.representation.DepartmentRepresentation;
import hr.prism.board.representation.DocumentRepresentation;
import hr.prism.board.representation.UniversityRepresentation;
import hr.prism.board.value.ResourceSearch;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static hr.prism.board.enums.CategoryType.MEMBER;
import static hr.prism.board.enums.MemberCategory.UNDERGRADUATE_STUDENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DepartmentMapperTest {

    @Mock
    private DocumentMapper documentMapper;

    @Mock
    private ResourceMapper resourceMapper;

    @Mock
    private UniversityMapper universityMapper;

    @Mock
    private OrganizationMapper organizationMapper;

    private DepartmentMapper departmentMapper;

    private University university;

    private Department department;

    private Document documentLogo;

    private UniversityRepresentation universityRepresentation;

    private DocumentRepresentation documentLogoRepresentation;

    @Before
    public void setUp() {
        university = new University();
        university.setId(1L);
        university.setHandle("university");

        documentLogo = new Document();
        documentLogo.setCloudinaryId("cloudinaryId");

        department = new Department();
        department.setId(1L);
        department.setParent(university);
        department.setDocumentLogo(documentLogo);
        department.setHandle("university/department");

        universityRepresentation =
            new UniversityRepresentation()
                .setId(1L);

        documentLogoRepresentation =
            new DocumentRepresentation()
                .setCloudinaryId("cloudinaryId");

        when(resourceMapper.apply(department, DepartmentRepresentation.class))
            .thenReturn(new DepartmentRepresentation());

        when(universityMapper.apply(university)).thenReturn(universityRepresentation);
        when(documentMapper.apply(documentLogo)).thenReturn(documentLogoRepresentation);
        when(resourceMapper.getHandle(department, university)).thenReturn("department");

        departmentMapper = new DepartmentMapper(documentMapper, resourceMapper, universityMapper, organizationMapper);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(documentMapper, resourceMapper, universityMapper, organizationMapper);
        reset(documentMapper, resourceMapper, universityMapper, organizationMapper);
    }

    @Test
    public void apply_success() {
        department.setSummary("summary");
        department.setCustomerId("customerId");
        department.setCategories(ImmutableSet.of(
            new ResourceCategory().setType(MEMBER).setName("UNDERGRADUATE_STUDENT")));

        DepartmentRepresentation departmentRepresentation = departmentMapper.apply(department);

        assertEquals(universityRepresentation, departmentRepresentation.getUniversity());
        assertEquals("summary", departmentRepresentation.getSummary());
        assertEquals(documentLogoRepresentation, departmentRepresentation.getDocumentLogo());
        assertEquals("department", departmentRepresentation.getHandle());
        assertEquals("customerId", departmentRepresentation.getCustomerId());
        assertThat(departmentRepresentation.getMemberCategories()).containsExactly(UNDERGRADUATE_STUDENT);

        verify(resourceMapper, times(1)).apply(department, DepartmentRepresentation.class);
        verify(universityMapper, times(1)).apply(university);
        verify(documentMapper, times(1)).apply(documentLogo);
        verify(resourceMapper, times(1)).getHandle(department, university);
    }

    @Test
    public void apply_successWhenNull() {
        assertNull(departmentMapper.apply((Department) null));
    }

    @Test
    public void apply_resourceSearch_success() {

    }

    @Test
    public void apply_resourceSearch_successWhenNull() {
        assertNull(departmentMapper.apply((ResourceSearch) null));
    }

    @Test
    public void applySmall_success() {

    }

    @Test
    public void applySmall_successWhenNull() {

    }

}
