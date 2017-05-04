package hr.prism.board.service;

import hr.prism.board.domain.Location;
import hr.prism.board.dto.LocationDTO;
import hr.prism.board.repository.LocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.math.RoundingMode;

@Service
@Transactional
public class LocationService {
    
    @Inject
    private LocationRepository locationRepository;
    
    public Location getOrCreateLocation(LocationDTO locationDTO) {
        Location location = locationRepository.findByGoogleId(locationDTO.getGoogleId());
        if (location == null) {
            location = new Location();
            location.setName(locationDTO.getName());
            location.setDomicile(locationDTO.getDomicile());
            location.setGoogleId(locationDTO.getGoogleId());
            location.setLatitude(locationDTO.getLatitude().setScale(18, RoundingMode.HALF_UP).stripTrailingZeros());
            location.setLongitude(locationDTO.getLongitude().setScale(18, RoundingMode.HALF_UP).stripTrailingZeros());
            return locationRepository.save(location);
        }
    
        return location;
    }
    
}
