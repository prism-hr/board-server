package hr.prism.board;

import com.google.common.base.Joiner;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;
import org.mockito.Mockito;
import org.springframework.mobile.device.Device;
import org.springframework.mobile.device.DevicePlatform;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import hr.prism.board.domain.User;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.dto.LocationDTO;
import hr.prism.board.dto.PostDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.ExistingRelation;
import hr.prism.board.enums.MemberCategory;
import hr.prism.board.representation.ActionRepresentation;
import hr.prism.board.representation.ChangeListRepresentation;
import hr.prism.board.representation.ResourceOperationRepresentation;
import hr.prism.board.representation.ResourceRepresentation;
import hr.prism.board.representation.UserRepresentation;
import hr.prism.board.util.ObjectUtils;

public class TestHelper {

    public static BoardDTO smallSampleBoard() {
        return new BoardDTO().setName("board");
    }

    public static BoardDTO sampleBoard() {
        return new BoardDTO()
            .setName("board")
            .setPostCategories(Arrays.asList("p1", "p2", "p3"));
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

    public static HttpServletRequest mockHttpServletRequest(String ipAddress) {
        return mockHttpServletRequest(ipAddress, null);
    }

    public static HttpServletRequest mockHttpServletRequest(String ipAddress, String proxyIpAddress) {
        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(httpServletRequest.getRemoteAddr()).thenReturn(proxyIpAddress == null ? ipAddress : proxyIpAddress);
        Mockito.when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn(Joiner.on(", ").skipNulls().join(ipAddress, proxyIpAddress));
        return httpServletRequest;
    }

    public static MockHttpServletResponse mockHttpServletResponse() throws IOException {
        MockHttpServletResponse httpServletResponse = Mockito.mock(MockHttpServletResponse.class);
        Mockito.doCallRealMethod().when(httpServletResponse).sendRedirect(Mockito.anyString());
        Mockito.when(httpServletResponse.getLocation()).thenCallRealMethod();
        return httpServletResponse;
    }

    public static Device mockDevice() {
        return new Device() {
            @Override
            public boolean isNormal() {
                return true;
            }

            @Override
            public boolean isMobile() {
                return false;
            }

            @Override
            public boolean isTablet() {
                return false;
            }

            @Override
            public DevicePlatform getDevicePlatform() {
                return null;
            }
        };
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

    public static void verifyNullableCount(Long expected, Long actual) {
        if (expected == 0L) {
            Assert.assertTrue(actual == null || actual == 0L);
        } else {
            Assert.assertEquals(expected, actual);
        }
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
        Assert.assertEquals(user.getEmailDisplay(), userRepresentation.getEmail());
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

    public static abstract class MockHttpServletResponse implements HttpServletResponse {

        private String location;

        public String getLocation() {
            return location;
        }

        @Override
        public void sendRedirect(String location) throws IOException {
            this.location = location;
        }

    }

}
