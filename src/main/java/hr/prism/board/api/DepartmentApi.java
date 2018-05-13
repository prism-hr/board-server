package hr.prism.board.api;

import hr.prism.board.domain.User;
import hr.prism.board.dto.DepartmentBadgeOptionsDTO;
import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.dto.DepartmentPatchDTO;
import hr.prism.board.mapper.DepartmentMapper;
import hr.prism.board.mapper.ResourceOperationMapper;
import hr.prism.board.representation.DepartmentDashboardRepresentation;
import hr.prism.board.representation.DepartmentRepresentation;
import hr.prism.board.representation.ResourceOperationRepresentation;
import hr.prism.board.service.DepartmentBadgeService;
import hr.prism.board.service.DepartmentDashboardService;
import hr.prism.board.service.DepartmentService;
import hr.prism.board.value.ResourceFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
public class DepartmentApi {

    private final DepartmentService departmentService;

    private final DepartmentDashboardService departmentDashboardService;

    private final DepartmentBadgeService departmentBadgeService;

    private final DepartmentMapper departmentMapper;

    private final ResourceOperationMapper resourceOperationMapper;

    @Inject
    public DepartmentApi(DepartmentService departmentService, DepartmentDashboardService departmentDashboardService,
                         DepartmentBadgeService departmentBadgeService, DepartmentMapper departmentMapper,
                         ResourceOperationMapper resourceOperationMapper) {
        this.departmentService = departmentService;
        this.departmentDashboardService = departmentDashboardService;
        this.departmentBadgeService = departmentBadgeService;
        this.departmentMapper = departmentMapper;
        this.resourceOperationMapper = resourceOperationMapper;
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/api/universities/{universityId}/departments", method = POST)
    public DepartmentRepresentation createDepartment(@AuthenticationPrincipal User user,
                                                     @PathVariable Long universityId,
                                                     @RequestBody @Valid DepartmentDTO departmentDTO) {
        return departmentMapper.apply(departmentService.createDepartment(user, universityId, departmentDTO));
    }

    @RequestMapping(value = "/api/universities/{universityId}/departments", method = GET)
    public List<DepartmentRepresentation> findDepartments(@PathVariable Long universityId,
                                                          @RequestParam String query) {
        return departmentService.findDepartment(universityId, query)
            .stream().map(departmentMapper::apply).collect(toList());
    }

    @RequestMapping(value = "/api/departments", method = GET)
    public List<DepartmentRepresentation> getDepartments(@AuthenticationPrincipal User user,
                                                         @ModelAttribute ResourceFilter filter) {
        return departmentService.getDepartments(user, filter).stream().map(departmentMapper).collect(toList());
    }

    @RequestMapping(value = "/api/departments/{departmentId}", method = GET)
    public DepartmentRepresentation getDepartment(@AuthenticationPrincipal User user, @PathVariable Long departmentId) {
        return departmentMapper.apply(departmentService.getById(user, departmentId));
    }

    @RequestMapping(value = "/api/departments", method = GET, params = "handle")
    public DepartmentRepresentation getDepartment(@AuthenticationPrincipal User user, @RequestParam String handle) {
        return departmentMapper.apply(departmentService.getByHandle(user, handle));
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/api/departments/{departmentId}/dashboard", method = GET)
    public DepartmentDashboardRepresentation getDepartmentDashboard(@AuthenticationPrincipal User user,
                                                                    @PathVariable Long departmentId) {
        return departmentMapper.apply(departmentDashboardService.getDepartmentDashboard(user, departmentId));
    }

    @RequestMapping(value = "/api/departments/{departmentId}/operations", method = GET)
    public List<ResourceOperationRepresentation> getDepartmentOperations(@PathVariable Long departmentId) {
        return departmentService.getDepartmentOperations(departmentId)
            .stream().map(resourceOperationMapper).collect(toList());
    }

    @RequestMapping(value = "/api/departments/{departmentId}", method = PATCH)
    public DepartmentRepresentation updateDepartment(@PathVariable Long departmentId,
                                                     @RequestBody @Valid DepartmentPatchDTO departmentDTO) {
        return departmentMapper.apply(departmentService.updateDepartment(departmentId, departmentDTO));
    }

    @RequestMapping(value = "/api/departments/{departmentId}/programs", method = GET)
    public List<String> getDepartmentPrograms(@PathVariable Long departmentId, @RequestParam String searchTerm) {
        return departmentService.findProgramsBySimilarName(departmentId, searchTerm);
    }

    @RequestMapping(value = "/api/departments/{departmentId}/badge", method = GET)
    public String getDepartmentBadge(@AuthenticationPrincipal User user, @PathVariable Long departmentId,
                                     @ModelAttribute DepartmentBadgeOptionsDTO options, HttpServletResponse response) {
        response.setHeader("X-Frame-Options", "ALLOW");
        return departmentBadgeService.getBadge(user, departmentId, options);
    }

}
