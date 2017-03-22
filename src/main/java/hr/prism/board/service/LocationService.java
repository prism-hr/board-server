package hr.prism.board.service;

import hr.prism.board.domain.Location;
import hr.prism.board.dto.LocationDTO;
import hr.prism.board.repository.LocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

@Service
@Transactional
public class LocationService {

    @Inject
    private LocationRepository locationRepository;

    public Location getOrCreateLocation(LocationDTO locationDTO) {
        Location foundLocation = locationRepository.findByGoogleId(locationDTO.getGoogleId());
        if (foundLocation != null) {
            return foundLocation;
        }
        Location location = new Location();
        location.setName(locationDTO.getName());
        location.setDomicile(locationDTO.getDomicile());
        location.setGoogleId(locationDTO.getGoogleId());
        location.setLatitude(locationDTO.getLatitude());
        location.setLongitude(locationDTO.getLongitude());
        return locationRepository.save(location);
    }

}
