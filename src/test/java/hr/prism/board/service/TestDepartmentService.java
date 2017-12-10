package hr.prism.board.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TestDepartmentService extends DepartmentService {

    @Override
    public LocalDateTime getBaseline() {
        return LocalDateTime.of(2017, 9, 1, 9, 0, 0);
    }

}
