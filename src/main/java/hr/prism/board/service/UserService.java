package hr.prism.board.service;

import com.stormpath.sdk.account.Account;
import hr.prism.board.domain.Document;
import hr.prism.board.domain.User;
import hr.prism.board.dto.DocumentDTO;
import hr.prism.board.dto.UserDTO;
import hr.prism.board.dto.UserPatchDTO;
import hr.prism.board.exception.ApiForbiddenException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

@Service
@Transactional
public class UserService {

    @Inject
    private UserRepository userRepository;

    @Inject
    private DocumentService documentService;

    public User findOne(Long id) {
        return userRepository.findOne(id);
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal == null || !(principal instanceof org.springframework.security.core.userdetails.User)) {
            return null;
        }

        org.springframework.security.core.userdetails.User user = (org.springframework.security.core.userdetails.User) principal;
        String username = user.getUsername();
        String stormpathId = username.substring(username.lastIndexOf('/') + 1);
        return userRepository.findByStormpathId(stormpathId);
    }

    public User getCurrentUserSecured() {
        User user = getCurrentUser();
        if (user == null) {
            throw new ApiForbiddenException(ExceptionCode.UNAUTHENTICATED_USER);
        }

        return user;
    }

    public User createUser(Account account) {
        User user = new User();
        user.setEmail(account.getEmail());
        user.setGivenName(account.getGivenName());
        user.setSurname(account.getSurname());

        String href = account.getHref();
        String stormpathId = href.substring(href.lastIndexOf('/') + 1);
        user.setStormpathId(stormpathId);
        return userRepository.save(user);
    }

    public User updateUser(UserPatchDTO userDTO) {
        User user = getCurrentUser();
        if (userDTO.getGivenName() != null) {
            user.setGivenName(userDTO.getGivenName().orElse(null));
        }
        if (userDTO.getSurname() != null) {
            user.setSurname(userDTO.getSurname().orElse(null));
        }
        if (userDTO.getDocumentImage() != null) {
            Document oldImage = user.getDocumentImage();
            DocumentDTO newImage = userDTO.getDocumentImage().orElse(null);
            if (oldImage != null && !oldImage.getId().equals(newImage.getId())) {
                documentService.deleteDocument(oldImage);
            }
            user.setDocumentImage(documentService.getOrCreateDocument(newImage));
        }

        userRepository.update(user);
        return user;
    }

    public User getOrCreateUser(UserDTO userDTO) {
        User user = userRepository.findByEmail(userDTO.getEmail());
        if(user != null) {
            return user;
        }
        user = new User();
        user.setEmail(userDTO.getEmail());
        user.setGivenName(userDTO.getGivenName());
        user.setSurname(userDTO.getSurname());
        user.setStormpathId("fake" + userDTO.getEmail()); // TODO drop this line
        return userRepository.save(user);
    }

    public User get(Long id) {
        return userRepository.findOne(id);
    }
}
