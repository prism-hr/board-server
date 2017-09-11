package hr.prism.board.dto;

public class AuthenticateDTO<T extends AuthenticateDTO> {

    private String uuid;

    public String getUuid() {
        return uuid;
    }

    @SuppressWarnings("unchecked")
    public T setUuid(String uuid) {
        this.uuid = uuid;
        return (T) this;
    }

}
