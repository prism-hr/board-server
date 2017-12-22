package hr.prism.board.authentication;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PusherAuthenticationDTO {

    @JsonProperty("socket_id")
    private String socketId;

    @JsonProperty("channel_name")
    private String channelName;

    public String getSocketId() {
        return socketId;
    }

    public void setSocketId(String socketId) {
        this.socketId = socketId;
    }

    public String getChannel() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

}
