package hr.prism.board.api;

import hr.prism.board.TestContext;
import hr.prism.board.dto.DocumentDTO;
import hr.prism.board.dto.UserPatchDTO;
import hr.prism.board.representation.UserRepresentation;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.util.Optional;

@TestContext
@RunWith(SpringRunner.class)
public class UserApiIT extends AbstractIT {

    @Inject
    private UserApi userApi;

    @Test
    public void shouldCreateAndUpdateUser() {
        testUserService.authenticate();

        DocumentDTO imageDTO = new DocumentDTO().setCloudinaryId("userImage").setCloudinaryUrl("userImage").setFileName("userImage");
        UserPatchDTO userDTO = new UserPatchDTO()
            .setGivenName(Optional.of("first"))
            .setSurname(Optional.of("second"))
            .setDocumentImage(Optional.of(imageDTO));
        userApi.updateUser(userDTO);

        UserRepresentation user = userApi.getCurrentUser();
        Assert.assertEquals("first", user.getGivenName());
        Assert.assertEquals("second", user.getSurname());
        verifyDocument(imageDTO, user.getDocumentImage());
    }

}
