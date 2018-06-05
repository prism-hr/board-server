package hr.prism.board.service;

import hr.prism.board.DbTestContext;
import hr.prism.board.domain.Location;
import hr.prism.board.dto.LocationDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@DbTestContext
@RunWith(SpringRunner.class)
@Sql(scripts = {"classpath:data/tearDown.sql", "classpath:data/locationService_setUp.sql"})
@Sql(scripts = {"classpath:data/tearDown.sql"}, executionPhase = AFTER_TEST_METHOD)
public class LocationServiceIT {

    @Inject
    private LocationService locationService;

    @Inject
    private ServiceHelper serviceHelper;

    @Test
    public void getOrCreateLocation_successWhenExisting() {
        Location location = locationService.getOrCreateLocation(
            new LocationDTO()
                .setName("london")
                .setDomicile("gb")
                .setGoogleId("google")
                .setLatitude(ONE)
                .setLongitude(ONE));
        verifyLocation(location, "london", "gb", "google", ONE, ONE);
    }

    @Test
    public void getOrCreateLocation_successWhenNew() {
        LocalDateTime baseline = LocalDateTime.now();
        LocationDTO locationDTO =
            new LocationDTO()
                .setName("new york")
                .setDomicile("us")
                .setGoogleId("new google")
                .setLatitude(TEN)
                .setLongitude(TEN);

        Location createdLocation = locationService.getOrCreateLocation(locationDTO);
        verifyLocation(createdLocation,
            "new york", "us", "new google", TEN, TEN);
        serviceHelper.verifyTimestamps(createdLocation, baseline);

        Location selectedLocation = locationService.getOrCreateLocation(locationDTO);
        verifyLocation(selectedLocation,
            "new york", "us", "new google", TEN, TEN);
        assertEquals(createdLocation, selectedLocation);
    }

    private void verifyLocation(Location location, String expectedName, String expectedDomicile,
                                String expectedGoogleId, BigDecimal expectedLatitude, BigDecimal expectedLongitude) {
        assertNotNull(location.getId());
        assertEquals(expectedName, location.getName());
        assertEquals(expectedDomicile, location.getDomicile());
        assertEquals(expectedGoogleId, location.getGoogleId());
        assertThat(expectedLatitude).isEqualByComparingTo(location.getLatitude());
        assertThat(expectedLongitude).isEqualByComparingTo(location.getLongitude());
    }

}
