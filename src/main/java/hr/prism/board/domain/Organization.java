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
        "SELECT id, name, logo, " +
            "IF(name LIKE :searchTermHard, 1, 0) AS similarityHard, " +
            "MATCH (name) AGAINST(:searchTermSoft IN BOOLEAN MODE) AS similaritySoft " +
            "FROM organization " +
            "HAVING similarityHard = 1 OR similaritySoft > 0 " +
            "ORDER BY similarityHard DESC, similaritySoft DESC, name " +
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
@SuppressWarnings("SqlResolve")
public class Organization extends BoardEntity {

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "logo")
    private String logo;

    @OneToMany(mappedBy = "organization")
    private Set<Post> posts = new HashSet<>();

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
