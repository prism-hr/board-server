package hr.prism.board.dto;

import hr.prism.board.enums.BadgeListType;
import hr.prism.board.enums.BadgeType;

import static hr.prism.board.enums.BadgeListType.STATIC;
import static hr.prism.board.enums.BadgeType.SIMPLE;

public class WidgetOptionsDTO extends ResourceDTO {

    private BadgeType badgeType = SIMPLE;

    private BadgeListType badgeListType = STATIC;

    private Integer postCount = 3;

    private Boolean preview = false;

    public BadgeType getBadgeType() {
        return badgeType;
    }

    public void setBadgeType(BadgeType badgeType) {
        this.badgeType = badgeType;
    }

    public BadgeListType getBadgeListType() {
        return badgeListType;
    }

    public void setBadgeListType(BadgeListType badgeListType) {
        this.badgeListType = badgeListType;
    }

    public Integer getPostCount() {
        return postCount;
    }

    public void setPostCount(Integer postCount) {
        this.postCount = postCount;
    }

    public Boolean getPreview() {
        return preview;
    }

    public void setPreview(Boolean preview) {
        this.preview = preview;
    }

}
