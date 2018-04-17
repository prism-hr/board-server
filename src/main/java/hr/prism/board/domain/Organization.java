package hr.prism.board.domain;

import hr.prism.board.value.OrganizationSearch;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "organization")
@NamedNativeQuery(
    name = "searchOrganizations",
    query =
        "SELECT resource.organization_name AS name, " +
            "resource.organization_logo AS logo, " +
            "IF(resource.organization_name LIKE :searchTermHard, 1, 0) AS similarityHard, " +
            "MATCH (resource.organization_name) AGAINST(:searchTermSoft IN BOOLEAN MODE) AS similaritySoft " +
            "FROM resource " +
            "WHERE resource.scope = :scope " +
            "GROUP BY resource.organization_name " +
            "HAVING similarityHard = 1 OR similaritySoft > 0 " +
            "ORDER BY similarityHard DESC, similaritySoft DESC, resource.organization_name " +
            "LIMIT 10",
    resultSetMapping = "searchOrganizations")
@SqlResultSetMapping(
    name = "searchOrganizations",
    classes = @ConstructorResult(
        targetClass = OrganizationSearch.class,
        columns = {
            @ColumnResult(name = "id", type = Long.class),
            @ColumnResult(name = "name", type = String.class),
            @ColumnResult(name = "logo", type = String.class)}))
public class Organization extends BoardEntity {

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "logo")
    private String logo;

    @OneToMany(mappedBy = "organization")
    Set<Post> posts = new HashSet<>();

    public String getName() {
        return name;
    }

    public Organization setName(String name) {
        this.name = name;
        return this;
    }

    public String getLogo() {
        return logo;
    }

    public Organization setLogo(String logo) {
        this.logo = logo;
        return this;
    }

    public Set<Post> getPosts() {
        return posts;
    }

    public Organization setPosts(Set<Post> posts) {
        this.posts = posts;
        return this;
    }

}
