package hr.prism.board.dto;

import hr.prism.board.enums.MemberCategory;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.List;

public class DepartmentDTO extends ResourceDTO<DepartmentDTO> {

    @Size(min = 3, max = 1000)
    private String summary;

    @Valid
    private DocumentDTO documentLogo;

    private List<MemberCategory> memberCategories;

    public String getSummary() {
        return summary;
    }

    public DepartmentDTO setSummary(String summary) {
        this.summary = summary;
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
