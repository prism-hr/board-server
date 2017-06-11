package hr.prism.board.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import hr.prism.board.authentication.AuthenticationToken;
import hr.prism.board.domain.Document;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.Role;
import hr.prism.board.domain.Scope;
import hr.prism.board.domain.User;
import hr.prism.board.dto.DocumentDTO;
import hr.prism.board.dto.UserDTO;
import hr.prism.board.dto.UserPatchDTO;
import hr.prism.board.enums.DocumentRequestState;
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.repository.UserRepository;
import hr.prism.board.service.cache.UserCacheService;

@Service
@Transactional
public class UserService {

    @Inject
    private UserRepository userRepository;

    @Inject
    private UserCacheService userCacheService;

    @Inject
    private DocumentService documentService;

    public User getCurrentUser() {
        return getCurrentUser(false);
    }

    public User getCurrentUserSecured() {
        return getCurrentUserSecured(false);
    }

    public User updateUser(UserPatchDTO userDTO) {
        User user = getCurrentUserSecured(true);
        Optional<String> givenNameOptional = userDTO.getGivenName();
        if (givenNameOptional != null) {
            user.setGivenName(givenNameOptional.orElse(user.getGivenName()));
        }

        Optional<String> surnameOptional = userDTO.getSurname();
        if (surnameOptional != null) {
            user.setSurname(surnameOptional.orElse(user.getSurname()));
        }

        Optional<DocumentDTO> documentImageOptional = userDTO.getDocumentImage();
        if (documentImageOptional != null) {
            Document oldImage = user.getDocumentImage();
            DocumentDTO newImage = documentImageOptional.orElse(null);
            if (newImage != null) {
                user.setDocumentImage(documentService.getOrCreateDocument(newImage));
            }

            if (oldImage != null && (newImage == null || !oldImage.getId().equals(newImage.getId()))) {
                documentService.deleteDocument(oldImage);
            }
        }

        Optional<DocumentRequestState> documentRequestStateOptional = userDTO.getDocumentImageRequestState();
        if (documentRequestStateOptional != null) {
            user.setDocumentImageRequestState(documentRequestStateOptional.orElse(user.getDocumentImageRequestState()));
        }

        return userCacheService.updateUser(user);
    }

    public User getOrCreateUser(UserDTO userDTO) {
        User user = userRepository.findByEmail(userDTO.getEmail());
        if (user == null) {
            user = new User();
            user.setEmail(userDTO.getEmail());
            user.setGivenName(userDTO.getGivenName());
            user.setSurname(userDTO.getSurname());
            return userRepository.save(user);
        }

        return user;
    }

    public List<User> findByResourceAndEnclosingScopeAndRole(Resource resource, Scope enclosingScope, Role role) {
        return userRepository.findByResourceAndEnclosingScopeAndRole(resource, enclosingScope, role);
    }

    private User getCurrentUser(boolean fresh) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }

        if (fresh) {
            return userCacheService.findOneFresh(((AuthenticationToken) authentication).getUserId());
        }

        return userCacheService.findOne(((AuthenticationToken) authentication).getUserId());
    }

    private User getCurrentUserSecured(boolean fresh) {
        User user = getCurrentUser(fresh);
        if (user == null) {
            throw new BoardForbiddenException(ExceptionCode.UNAUTHENTICATED_USER);
        }

        return user;
    }

}
