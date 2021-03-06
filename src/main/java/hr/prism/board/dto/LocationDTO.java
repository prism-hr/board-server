package hr.prism.board.dto;

import hr.prism.board.definition.LocationDefinition;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

public class LocationDTO implements LocationDefinition {

    @NotNull
    @Size(max = 255)
    private String name;

    @NotNull
    @Size(min = 1, max = 3)
    private String domicile;

    @NotNull
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

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(googleId)
            .toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        LocationDTO that = (LocationDTO) other;
        return new EqualsBuilder()
            .append(googleId, that.googleId)
            .isEquals();
    }

}
