package hr.prism.board.dto;

import hr.prism.board.enums.BadgeListType;
import hr.prism.board.enums.BadgeType;

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

    @SuppressWarnings("unused")
    public void setBadgeType(BadgeType badgeType) {
        this.badgeType = badgeType;
    }

    @SuppressWarnings("unused")
    public BadgeListType getBadgeListType() {
        return badgeListType;
    }

    @SuppressWarnings("unused")
    public void setBadgeListType(BadgeListType badgeListType) {
        this.badgeListType = badgeListType;
    }

    public Integer getPostCount() {
        return postCount;
    }

    @SuppressWarnings("unused")
    public void setPostCount(Integer postCount) {
        this.postCount = postCount;
    }

    public Boolean getPreview() {
        return preview;
    }

    @SuppressWarnings("unused")
    public void setPreview(Boolean preview) {
        this.preview = preview;
    }

}
