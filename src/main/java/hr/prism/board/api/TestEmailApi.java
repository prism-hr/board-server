package hr.prism.board.api;

import hr.prism.board.representation.TestEmailMessageRepresentation;
import hr.prism.board.service.TestEmailService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;

@RestController
@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
public class TestEmailApi {

    @Inject
    private TestEmailService testEmailService;

    @RequestMapping(value = "/api/test/emails", method = RequestMethod.GET)
    public List<TestEmailMessageRepresentation> getMessages(@RequestParam(required = false) String email) {
        return email == null ? testEmailService.findAll() : testEmailService.findByUserEmail(email);
    }

}
