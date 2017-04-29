package hr.prism.board;

import hr.prism.board.domain.User;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.dto.LocationDTO;
import hr.prism.board.dto.PostDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.ExistingRelation;
import hr.prism.board.representation.ResourceChangeListRepresentation;
import hr.prism.board.representation.ResourceOperationRepresentation;
import hr.prism.board.representation.UserRepresentation;
import org.junit.Assert;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;

public class TestHelper {
    
    public static void verifyUser(User user, UserRepresentation userRepresentation) {
        Assert.assertEquals(user.getId(), userRepresentation.getId());
        Assert.assertEquals(user.getGivenName(), userRepresentation.getGivenName());
        Assert.assertEquals(user.getSurname(), userRepresentation.getSurname());
        Assert.assertEquals(user.getEmail(), userRepresentation.getEmail());
    }
    
    public static BoardDTO sampleBoard() {
        return new BoardDTO()
            .setName("board")
            .setPostCategories(new ArrayList<>())
            .setDepartment(new DepartmentDTO()
                .setName("department"));
    }
    
    public static PostDTO samplePost() {
        return new PostDTO()
            .setName("post")
            .setDescription("description")
            .setOrganizationName("organization name")
            .setLocation(new LocationDTO()
                .setName("krakow")
                .setDomicile("PL")
                .setGoogleId("sss")
                .setLatitude(BigDecimal.ONE)
                .setLongitude(BigDecimal.ONE))
            .setApplyWebsite("http://www.google.co.uk")
            .setPostCategories(Collections.emptyList())
            .setMemberCategories(Collections.emptyList())
            .setExistingRelation(ExistingRelation.STUDENT)
            .setLiveTimestamp(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
            .setDeadTimestamp(LocalDateTime.now().plusWeeks(1L).truncatedTo(ChronoUnit.SECONDS));
    }
    
    public static void verifyResourceOperation(ResourceOperationRepresentation resourceOperationR, Action expectedAction, User expectedUser,
        ResourceChangeListRepresentation expectedChanges) {
        Assert.assertEquals(expectedAction, resourceOperationR.getAction());
        verifyUser(expectedUser, resourceOperationR.getUser());
        Assert.assertEquals(expectedChanges, resourceOperationR.getChangeList());
    }
    
}
