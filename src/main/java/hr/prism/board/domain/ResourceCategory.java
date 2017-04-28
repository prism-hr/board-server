package hr.prism.board.domain;

import hr.prism.board.enums.CategoryType;
import org.apache.commons.lang3.ObjectUtils;

import javax.persistence.*;

@Entity
@Table(name = "resource_category")
public class ResourceCategory extends BoardEntity implements Comparable<ResourceCategory> {
    
    @ManyToOne
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;
    
    @Column(name = "name", nullable = false, length = 50)
    private String name;
    
    @Column(name = "type", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private CategoryType type;
    
    @Column(name = "ordinal")
    private Integer ordinal;
    
    public Resource getResource() {
        return resource;
    }
    
    public ResourceCategory setResource(Resource resource) {
        this.resource = resource;
        return this;
    }
    
    public String getName() {
        return name;
    }
    
    public ResourceCategory setName(String name) {
        this.name = name;
        return this;
    }
    
    public CategoryType getType() {
        return type;
    }
    
    public ResourceCategory setType(CategoryType type) {
        this.type = type;
        return this;
    }
    
    public Integer getOrdinal() {
        return ordinal;
    }
    
    public ResourceCategory setOrdinal(Integer ordinal) {
        this.ordinal = ordinal;
        return this;
    }
    
    @Override
    public int compareTo(ResourceCategory o) {
        int compare = ObjectUtils.compare(type, o.getType());
        return compare == 0 ? ObjectUtils.compare(ordinal, o.getOrdinal()) : compare;
    }
    
}
