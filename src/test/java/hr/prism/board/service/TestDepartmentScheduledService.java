package hr.prism.board.service;

import hr.prism.board.service.scheduled.DepartmentScheduledService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TestDepartmentScheduledService extends DepartmentScheduledService {

    @Override
    public LocalDateTime getBaseline() {
        return LocalDateTime.of(2017, 9, 1, 9, 0, 0);
    }

}
