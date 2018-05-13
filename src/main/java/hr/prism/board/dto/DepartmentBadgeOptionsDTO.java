package hr.prism.board.dto;

import hr.prism.board.enums.BadgeListType;
import hr.prism.board.enums.BadgeType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import static hr.prism.board.enums.BadgeListType.STATIC;
import static hr.prism.board.enums.BadgeType.SIMPLE;

public class DepartmentBadgeOptionsDTO {

    private BadgeType badgeType = SIMPLE;

    private BadgeListType badgeListType = STATIC;

    private Integer postCount = 3;

    private Boolean preview = false;

    @SuppressWarnings("unused")
    public BadgeType getBadgeType() {
        return badgeType;
    }

    public DepartmentBadgeOptionsDTO setBadgeType(BadgeType badgeType) {
        this.badgeType = badgeType;
        return this;
    }

    @SuppressWarnings("unused")
    public BadgeListType getBadgeListType() {
        return badgeListType;
    }

    public DepartmentBadgeOptionsDTO setBadgeListType(BadgeListType badgeListType) {
        this.badgeListType = badgeListType;
        return this;
    }

    public Integer getPostCount() {
        return postCount;
    }

    public DepartmentBadgeOptionsDTO setPostCount(Integer postCount) {
        this.postCount = postCount;
        return this;
    }

    public Boolean getPreview() {
        return preview;
    }

    public DepartmentBadgeOptionsDTO setPreview(Boolean preview) {
        this.preview = preview;
        return this;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(badgeType)
            .append(badgeListType)
            .append(postCount)
            .append(preview)
            .toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        DepartmentBadgeOptionsDTO that = (DepartmentBadgeOptionsDTO) other;
        return new EqualsBuilder()
            .append(badgeType, that.badgeType)
            .append(badgeListType, that.badgeListType)
            .append(postCount, that.postCount)
            .append(preview, that.preview)
            .isEquals();
    }

}
