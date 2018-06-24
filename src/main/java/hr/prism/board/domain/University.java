package hr.prism.board.domain;

import hr.prism.board.enums.Scope;
import hr.prism.board.value.ResourceSearch;

import javax.persistence.*;

import static hr.prism.board.enums.Scope.Value.UNIVERSITY;

@Entity
@DiscriminatorValue(value = UNIVERSITY)
@NamedNativeQuery(
    name = "universitySearch",
    query =
        "SELECT resource.id as id, resource.name as name, document_logo.cloudinary_id as documentCloudinaryId, " +
            "document_logo.cloudinary_url as documentCloudinaryUrl, document_logo.file_name as documentFileName, " +
            "IF(resource.name LIKE :searchTermHard, 1, 0) AS similarityHard, " +
            "MATCH (resource.name) AGAINST(:searchTermSoft IN BOOLEAN MODE) AS similaritySoft " +
            "FROM resource " +
            "LEFT JOIN document AS document_logo " +
            "ON resource.document_logo_id = document_logo.id " +
            "WHERE resource.scope = 'UNIVERSITY' " +
            "AND resource.state = 'ACCEPTED' " +
            "HAVING similarityHard = 1 OR similaritySoft > 0 " +
            "ORDER BY similarityHard DESC, similaritySoft DESC, resource.name " +
            "LIMIT 10",
    resultSetMapping = "universitySearch")
@SqlResultSetMapping(
    name = "universitySearch",
    classes = @ConstructorResult(
        targetClass = ResourceSearch.class,
        columns = {
            @ColumnResult(name = "id", type = Long.class),
            @ColumnResult(name = "name", type = String.class),
            @ColumnResult(name = "documentCloudinaryId", type = String.class),
            @ColumnResult(name = "documentCloudinaryUrl", type = String.class),
            @ColumnResult(name = "documentFileName", type = String.class)}))
@SuppressWarnings("SqlResolve")
public class University extends Resource {

    public University() {
        setScope(Scope.UNIVERSITY);
    }

    @Column(name = "homepage")
    private String homepage;

    public String getHomepage() {
        return homepage;
    }

    @SuppressWarnings("unused")
    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

}
