package hr.prism.board.service;

import hr.prism.board.dao.OrganizationDAO;
import hr.prism.board.dto.OrganizationDTO;
import hr.prism.board.repository.OrganizationRepository;
import hr.prism.board.value.OrganizationSearch;
import hr.prism.board.value.OrganizationStatistics;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

@Service
@Transactional
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    private final OrganizationDAO organizationDAO;

    @Inject
    public OrganizationService(OrganizationRepository organizationRepository, OrganizationDAO organizationDAO) {
        this.organizationRepository = organizationRepository;
        this.organizationDAO = organizationDAO;
    }

    public hr.prism.board.domain.Organization getOrCreateOrganization(OrganizationDTO organizationDTO) {
        Long id = organizationDTO.getId();
        if (id != null) {
            return organizationRepository.findOne(id);
        }

        String name = organizationDTO.getName();
        hr.prism.board.domain.Organization organization = organizationRepository.findByName(name);
        if (organization != null) {
            return organization;
        }

        return organizationRepository.save(
            new hr.prism.board.domain.Organization()
                .setName(name)
                .setLogo(organizationDTO.getLogo()));
    }

    public List<OrganizationSearch> findOrganizations(String searchTerm) {
        return organizationDAO.findOrganizations(searchTerm);
    }

    public List<OrganizationStatistics> getOrganizationStatistics(Long departmentId) {
        return organizationRepository.findOrganizationStatisticsByDepartmentId(departmentId);
    }

}
