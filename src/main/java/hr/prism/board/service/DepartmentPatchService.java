package hr.prism.board.service;

import hr.prism.board.domain.Department;
import hr.prism.board.enums.MemberCategory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

import static hr.prism.board.enums.CategoryType.MEMBER;
import static hr.prism.board.enums.MemberCategory.toStrings;

@Service
@Transactional
public class DepartmentPatchService extends ResourcePatchService<Department> {

    @Inject
    public DepartmentPatchService(LocationService locationService, DocumentService documentService,
                                  ResourceService resourceService) {
        super(locationService, documentService, resourceService);
    }

    @SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "OptionalAssignedToNull"})
    public void patchMemberCategories(Department department, Optional<List<MemberCategory>> categories) {

        Optional<List<String>> categoryStrings = Optional.empty();
        if (categories == null) {
            categoryStrings = null;
        } else if (categories.isPresent()) {
            categoryStrings = Optional.of(toStrings(categories.get()));
        }

        patchCategories(department, MEMBER, categoryStrings);
    }

}
