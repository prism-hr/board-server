package hr.prism.board.enums;

import com.google.common.collect.ImmutableList;

import java.util.List;

public enum ResourceEvent {

    VIEW,
    REFERRAL,
    RESPONSE;

    public static List<ResourceEvent> RESPONSE_EVENTS = ImmutableList.of(REFERRAL, RESPONSE);

}
