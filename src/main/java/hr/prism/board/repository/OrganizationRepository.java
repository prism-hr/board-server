package hr.prism.board.repository;

import hr.prism.board.domain.Organization;
import hr.prism.board.value.OrganizationStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    Organization findByName(String name);

    @Query(value =
        "select new hr.prism.board.value.OrganizationStatistics(organization.id, organization.name, " +
            "organization.logo, count(post.id), max(post.createdTimestamp), coalesce(sum(post.viewCount), 0), " +
            "coalesce(sum(post.referralCount), 0), coalesce(sum(post.responseCount), 0)) " +
            "from Organization organization " +
            "inner join organization.posts post " +
            "inner join post.parent board " +
            "where board.parent.id = :departmentId " +
            "group by organization.name " +
            "order by mostRecentPost desc")
    @SuppressWarnings("SpringDataRepositoryMethodReturnTypeInspection")
    List<OrganizationStatistics> findOrganizationStatisticsByDepartmentId(@Param("departmentId") Long departmentId);

}
