package hr.prism.board.mapper;

import hr.prism.board.domain.Location;
import hr.prism.board.representation.LocationRepresentation;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class LocationMapper implements Function<Location, LocationRepresentation> {

    @Override
    public LocationRepresentation apply(Location location) {
        if (location == null) {
            return null;
        }
        return new LocationRepresentation()
            .setName(location.getName())
            .setDomicile(location.getDomicile())
            .setGoogleId(location.getGoogleId())
            .setLatitude(location.getLatitude())
            .setLongitude(location.getLongitude());
    }

}
