package hr.prism.board.api;

import hr.prism.board.representation.TestEmailMessageRepresentation;
import hr.prism.board.service.TestEmailService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
public class TestEmailApi {

    private final TestEmailService testEmailService;

    @Inject
    public TestEmailApi(TestEmailService testEmailService) {
        this.testEmailService = testEmailService;
    }

    @RequestMapping(value = "/api/test/emails", method = GET)
    public List<TestEmailMessageRepresentation> getMessages(@RequestParam(required = false) String email) {
        return email == null ? testEmailService.findAll() : testEmailService.findByUserEmail(email);
    }

}
