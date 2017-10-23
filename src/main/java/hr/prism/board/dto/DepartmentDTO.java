package hr.prism.board.dto;

import hr.prism.board.enums.MemberCategory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

public class DepartmentDTO extends ResourceDTO<DepartmentDTO> {

    private Long id;

    @Valid
    @NotNull
    private UniversityDTO university;

    @Valid
    private DocumentDTO documentLogo;

    private List<MemberCategory> memberCategories;

    public Long getId() {
        return id;
    }

    public DepartmentDTO setId(Long id) {
        this.id = id;
        return this;
    }

    public UniversityDTO getUniversity() {
        return university;
    }

    public DepartmentDTO setUniversity(UniversityDTO university) {
        this.university = university;
        return this;
    }

    public DocumentDTO getDocumentLogo() {
        return documentLogo;
    }

    public DepartmentDTO setDocumentLogo(DocumentDTO documentLogo) {
        this.documentLogo = documentLogo;
        return this;
    }

    public List<MemberCategory> getMemberCategories() {
        return memberCategories;
    }

    public DepartmentDTO setMemberCategories(List<MemberCategory> memberCategories) {
        this.memberCategories = memberCategories;
        return this;
    }

}
