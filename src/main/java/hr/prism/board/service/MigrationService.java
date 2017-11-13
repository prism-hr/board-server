package hr.prism.board.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;

@Service
public class MigrationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MigrationService.class);

    @Inject
    private UniversityService universityService;

    private volatile boolean migrating = true;

    @Scheduled(initialDelay = 10000, fixedDelay = 10000)
    public synchronized void migrate() throws IOException {
        if (this.migrating) {
            try {
                LOGGER.info("Starting migrations");
                for (Long universityId : universityService.findAllIds()) {
                    universityService.migrate(universityId);
                }
            } catch (Throwable t) {
                LOGGER.error("Error performing migrations", t);
            } finally {
                LOGGER.info("Finished migrations");
                this.migrating = false;
            }
        }
    }

}
