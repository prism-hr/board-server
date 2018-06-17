package hr.prism.board.mapper;

import hr.prism.board.domain.Location;
import hr.prism.board.representation.LocationRepresentation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class LocationMapperTest {

    private LocationMapper locationMapper = new LocationMapper();

    @Test
    public void apply_success() {
        Location location = new Location();
        location.setName("name");
        location.setDomicile("domicile");
        location.setGoogleId("googleId");
        location.setLatitude(ZERO);
        location.setLongitude(ONE);

        LocationRepresentation locationRepresentation = locationMapper.apply(location);
        assertEquals("name", locationRepresentation.getName());
        assertEquals("domicile", locationRepresentation.getDomicile());
        assertEquals("googleId", locationRepresentation.getGoogleId());
        assertThat(locationRepresentation.getLatitude()).isEqualByComparingTo("0.00");
        assertThat(locationRepresentation.getLongitude()).isEqualByComparingTo("1.00");
    }

}
