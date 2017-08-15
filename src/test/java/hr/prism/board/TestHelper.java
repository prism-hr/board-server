package hr.prism.board;

import hr.prism.board.domain.User;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.dto.LocationDTO;
import hr.prism.board.dto.PostDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.ExistingRelation;
import hr.prism.board.enums.MemberCategory;
import hr.prism.board.representation.*;
import hr.prism.board.util.ObjectUtils;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class TestHelper {

    public static BoardDTO smallSampleBoard() {
        return new BoardDTO()
            .setName("board")
            .setDepartment(new DepartmentDTO()
                .setName("department"));
    }

    public static DepartmentDTO sampleDepartment() {
        return new DepartmentDTO()
            .setName("department")
            .setMemberCategories(Arrays.asList(MemberCategory.UNDERGRADUATE_STUDENT, MemberCategory.MASTER_STUDENT));
    }

    public static BoardDTO sampleBoard() {
        return new BoardDTO()
            .setName("board")
            .setPostCategories(Arrays.asList("p1", "p2", "p3"))
            .setDepartment(new DepartmentDTO()
                .setName("department")
                .setMemberCategories(Arrays.asList(MemberCategory.UNDERGRADUATE_STUDENT, MemberCategory.MASTER_STUDENT)));
    }

    public static PostDTO smallSamplePost() {
        return new PostDTO()
            .setName("post")
            .setSummary("summary")
            .setDescription("description")
            .setOrganizationName("organization name")
            .setLocation(new LocationDTO()
                .setName("krakow")
                .setDomicile("PL")
                .setGoogleId("sss")
                .setLatitude(BigDecimal.ONE)
                .setLongitude(BigDecimal.ONE))
            .setApplyWebsite("http://www.google.co.uk")
            .setExistingRelation(ExistingRelation.STUDENT)
            .setExistingRelationExplanation(ObjectUtils.orderedMap("studyLevel", "MASTER"))
            .setLiveTimestamp(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
            .setDeadTimestamp(LocalDateTime.now().plusWeeks(1L).truncatedTo(ChronoUnit.SECONDS));
    }

    public static PostDTO samplePost() {
        return new PostDTO()
            .setName("post")
            .setSummary("summary")
            .setOrganizationName("organization name")
            .setLocation(new LocationDTO()
                .setName("krakow")
                .setDomicile("PL")
                .setGoogleId("sss")
                .setLatitude(BigDecimal.ONE)
                .setLongitude(BigDecimal.ONE))
            .setApplyWebsite("http://www.google.co.uk")
            .setPostCategories(Arrays.asList("p1", "p2"))
            .setMemberCategories(Arrays.asList(MemberCategory.UNDERGRADUATE_STUDENT, MemberCategory.MASTER_STUDENT))
            .setExistingRelation(ExistingRelation.STUDENT)
            .setExistingRelationExplanation(ObjectUtils.orderedMap("studyLevel", "MASTER"))
            .setLiveTimestamp(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
            .setDeadTimestamp(LocalDateTime.now().plusWeeks(1L).truncatedTo(ChronoUnit.SECONDS));
    }

    public static <T extends ResourceRepresentation<T>> void verifyResources(List<T> resources, List<String> expectedNames, ExpectedActions expectedActions) {
        int resourcesSize = resources.size();
        Assert.assertEquals(expectedNames.size(), resourcesSize);
        if (expectedActions == null) {
            if (resourcesSize > 0) {
                Assert.fail();
            }

            return;
        }

        Collection<Action> expectedActionsDefault = expectedActions.get("default");
        if (CollectionUtils.isEmpty(expectedActionsDefault) && expectedActions.size() < resourcesSize) {
            Assert.fail();
        }

        for (int i = 0; i < resources.size(); i++) {
            T resource = resources.get(i);
            String expectedName = expectedNames.get(i);
            Assert.assertEquals(expectedName, resource.getName());

            Collection<Action> expectedActionsCustom = expectedActions.get(expectedName);
            if (CollectionUtils.isEmpty(expectedActionsCustom)) {
                if (expectedActionsDefault == null) {
                    Assert.fail();
                }

                Assert.assertEquals(expectedActionsDefault, resource.getActions().stream().map(ActionRepresentation::getAction).collect(Collectors.toList()));
            } else {
                Assert.assertEquals(expectedActionsCustom, resource.getActions().stream().map(ActionRepresentation::getAction).collect(Collectors.toList()));
            }
        }
    }

    public static void verifyResourceOperation(ResourceOperationRepresentation resourceOperationR, Action expectedAction) {
        verifyResourceOperation(resourceOperationR, expectedAction, null, null, null);
    }

    public static void verifyResourceOperation(ResourceOperationRepresentation resourceOperationR, Action expectedAction, User expectedUser) {
        verifyResourceOperation(resourceOperationR, expectedAction, expectedUser, null, null);
    }

    public static void verifyResourceOperation(ResourceOperationRepresentation resourceOperationR, Action expectedAction, User expectedUser,
                                               ChangeListRepresentation expectedChanges) {
        verifyResourceOperation(resourceOperationR, expectedAction, expectedUser, expectedChanges, null);
    }

    public static void verifyResourceOperation(ResourceOperationRepresentation resourceOperationR, Action expectedAction, User expectedUser,
                                               String expectedComment) {
        verifyResourceOperation(resourceOperationR, expectedAction, expectedUser, null, expectedComment);
    }

    public static String toString(LocalDateTime baseline) {
        if (baseline == null) {
            return null;
        }

        baseline = baseline.truncatedTo(ChronoUnit.SECONDS);
        int seconds = baseline.getSecond();
        if (seconds > 0) {
            return baseline.toString();
        }

        return baseline.toString() + ":00";
    }


    private static void verifyResourceOperation(ResourceOperationRepresentation resourceOperationR, Action expectedAction, User expectedUser,
                                                ChangeListRepresentation expectedChanges, String expectedComment) {
        Assert.assertEquals(expectedAction, resourceOperationR.getAction());

        if (expectedUser == null) {
            Assert.assertNull(resourceOperationR.getUser());
        } else {
            verifyUser(expectedUser, resourceOperationR.getUser());
        }

        Assert.assertEquals(expectedChanges, resourceOperationR.getChangeList());

        Assert.assertEquals(expectedComment, resourceOperationR.getComment());
    }

    private static void verifyUser(User user, UserRepresentation userRepresentation) {
        Assert.assertEquals(user.getId(), userRepresentation.getId());
        Assert.assertEquals(user.getGivenName(), userRepresentation.getGivenName());
        Assert.assertEquals(user.getSurname(), userRepresentation.getSurname());
        Assert.assertEquals(user.getEmail(), userRepresentation.getEmail());
    }

    public static class ExpectedActions extends LinkedHashMap<String, List<Action>> {

        public ExpectedActions add(List<Action> values) {
            add("default", values);
            return this;
        }

        public ExpectedActions addAll(List<String> keys, List<Action> values) {
            keys.forEach(key -> add(key, values));
            return this;
        }

        private ExpectedActions add(String key, List<Action> values) {
            values.sort(Comparator.naturalOrder());
            super.put(key, values);
            return this;
        }

    }

}
