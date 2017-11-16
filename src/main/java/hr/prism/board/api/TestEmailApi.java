package hr.prism.board.api;

import hr.prism.board.representation.TestEmailMessageRepresentation;
import hr.prism.board.service.TestEmailService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;

@RestController
@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
public class TestEmailApi {

    @Inject
    private TestEmailService testEmailService;

    @RequestMapping(value = "/api/test/emails", method = RequestMethod.GET)
    public List<TestEmailMessageRepresentation> getMessages() {
        return testEmailService.findAll();
    }

    @RequestMapping(value = "/api/test/emails/{userId}", method = RequestMethod.GET)
    public List<TestEmailMessageRepresentation> getMessages(@PathVariable Long userId) {
        return testEmailService.findByUserId(userId);
    }

}
