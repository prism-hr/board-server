package hr.prism.board.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import javax.inject.Inject;

import hr.prism.board.domain.TestEmail;
import hr.prism.board.domain.User;
import hr.prism.board.mapper.UserMapper;
import hr.prism.board.repository.TestEmailRepository;
import hr.prism.board.representation.TestEmailMessageRepresentation;

@Service
@Transactional
@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
public class TestEmailService {

    @Inject
    private TestEmailRepository testEmailRepository;

    @Inject
    private UserMapper userMapper;

    public List<TestEmailMessageRepresentation> findAll() {
        return testEmailRepository.findAllMessages();
    }

    public List<TestEmailMessageRepresentation> findByUserEmail(String email) {
        return testEmailRepository.findMessagesByUserEmail(email);
    }

    public void createTestEmail(User user, String subject, String content, List<String> attachments, Long creatorId) {
        TestEmail testEmail = new TestEmail();
        testEmail.setUser(user);
        testEmail.setMessage(
            new TestEmailMessageRepresentation()
                .setRecipient(userMapper.apply(user))
                .setSubject(subject)
                .setContent(content)
                .setAttachments(attachments));

        testEmail.setCreatorId(creatorId);
        testEmailRepository.save(testEmail);
    }

}
