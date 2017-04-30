package hr.prism.board.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

public class LocationDTO {
    
    @Size(max = 255)
    private String name;
    
    @Size(min = 1, max = 3)
    private String domicile;
    
    @Size(min = 1, max = 255)
    private String googleId;
    
    @NotNull
    private BigDecimal latitude;
    
    @NotNull
    private BigDecimal longitude;
    
    public String getName() {
        return name;
    }
    
    public LocationDTO setName(String name) {
        this.name = name;
        return this;
    }
    
    public String getDomicile() {
        return domicile;
    }
    
    public LocationDTO setDomicile(String domicile) {
        this.domicile = domicile;
        return this;
    }
    
    public String getGoogleId() {
        return googleId;
    }
    
    public LocationDTO setGoogleId(String googleId) {
        this.googleId = googleId;
        return this;
    }
    
    public BigDecimal getLatitude() {
        return latitude;
    }
    
    public LocationDTO setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
        return this;
    }
    
    public BigDecimal getLongitude() {
        return longitude;
    }
    
    public LocationDTO setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
        return this;
    }
}
