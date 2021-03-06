package hr.prism.board.domain;

import hr.prism.board.definition.LocationDefinition;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "location")
public class Location extends BoardEntity implements LocationDefinition {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "domicile", nullable = false)
    private String domicile;

    @Column(name = "google_id", nullable = false)
    private String googleId;

    @Column(name = "latitude", nullable = false)
    private BigDecimal latitude;

    @Column(name = "longitude", nullable = false)
    private BigDecimal longitude;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDomicile() {
        return domicile;
    }

    public void setDomicile(String domicile) {
        this.domicile = domicile;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
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

        Location location = (Location) other;
        return new EqualsBuilder()
            .append(googleId, location.googleId)
            .isEquals();
    }

    @Override
    public String toString() {
        return name;
    }

}
