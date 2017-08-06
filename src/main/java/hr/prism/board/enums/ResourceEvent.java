package hr.prism.board.enums;

import java.util.Arrays;
import java.util.List;

public enum ResourceEvent {

    VIEW,
    CLICK,
    DOWNLOAD,
    EMAIL,
    SHARE;

    public static List<ResourceEvent> REFERRAL_EVENTS = Arrays.asList(CLICK, DOWNLOAD);

    public static List<ResourceEvent> RESPONSE_EVENTS = Arrays.asList(CLICK, DOWNLOAD, EMAIL);

}
