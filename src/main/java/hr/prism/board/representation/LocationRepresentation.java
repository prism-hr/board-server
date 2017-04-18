package hr.prism.board.representation;

import java.math.BigDecimal;
import java.util.Objects;

public class LocationRepresentation {
    
    private String name;
    
    private String domicile;
    
    private String googleId;
    
    private BigDecimal latitude;
    
    private BigDecimal longitude;
    
    public String getName() {
        return name;
    }
    
    public LocationRepresentation setName(String name) {
        this.name = name;
        return this;
    }
    
    public String getDomicile() {
        return domicile;
    }
    
    public LocationRepresentation setDomicile(String domicile) {
        this.domicile = domicile;
        return this;
    }
    
    public String getGoogleId() {
        return googleId;
    }
    
    public LocationRepresentation setGoogleId(String googleId) {
        this.googleId = googleId;
        return this;
    }
    
    public BigDecimal getLatitude() {
        return latitude;
    }
    
    public LocationRepresentation setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
        return this;
    }
    
    public BigDecimal getLongitude() {
        return longitude;
    }
    
    public LocationRepresentation setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
        return this;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(googleId);
    }
    
    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        
        return Objects.equals(googleId, ((LocationRepresentation) object).getGoogleId());
    }
    
}
