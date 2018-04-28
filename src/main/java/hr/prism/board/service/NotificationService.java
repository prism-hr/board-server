package hr.prism.board.service;

import com.sendgrid.*;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.Notification;
import hr.prism.board.exception.BoardException;
import hr.prism.board.notification.BoardAttachments;
import hr.prism.board.notification.property.NotificationProperty;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StrSubstitutor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static com.sendgrid.Method.POST;
import static hr.prism.board.exception.ExceptionCode.MISSING_NOTIFICATION_PROPERTY;
import static hr.prism.board.exception.ExceptionCode.UNDELIVERABLE_NOTIFICATION;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.slf4j.LoggerFactory.getLogger;

@Service
public class NotificationService {

    private static final Logger LOGGER = getLogger(NotificationService.class);

    private final Map<Notification, String> subjects = new TreeMap<>();

    private final Map<Notification, String> contents = new TreeMap<>();

    private final Map<Notification, Map<String, NotificationProperty>> properties = new TreeMap<>();

    private final boolean mailOn;

    private final String senderEmail;

    private final TestEmailService testEmailService;

    private final SendGrid sendGrid;

    private final ApplicationContext applicationContext;

    @Inject
    public NotificationService(@Value("${mail.on}") boolean mailOn, @Value("${system.email}") String senderEmail,
                               TestEmailService testEmailService, SendGrid sendGrid,
                               ApplicationContext applicationContext) {
        this.mailOn = mailOn;
        this.senderEmail = senderEmail;
        this.testEmailService = testEmailService;
        this.sendGrid = sendGrid;
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void postConstruct() throws IOException {
        Map<String, NotificationProperty> propertiesMap =
            applicationContext.getBeansOfType(NotificationProperty.class).values().stream()
                .collect(toMap(NotificationProperty::getKey, identity()));

        String header = toString(applicationContext.getResource("classpath:notification/content/header.html"));
        String footer = toString(applicationContext.getResource("classpath:notification/content/footer.html"));

        for (Notification notification : Notification.values()) {
            String subject = toString(
                applicationContext.getResource("classpath:notification/subject/" + notification + ".html"));
            String content = toString(
                applicationContext.getResource("classpath:notification/content/" + notification + ".html"))
                .replace("${header}", header).replace("${footer}", footer);

            Set<String> placeholders = new HashSet<>();
            placeholders.addAll(indexTemplate(notification, subject, this.subjects));
            placeholders.addAll(indexTemplate(notification, content, this.contents));

            Map<String, NotificationProperty> properties = new TreeMap<>();
            for (String placeholder : placeholders) {
                NotificationProperty property = propertiesMap.get(placeholder);
                if (property == null) {
                    throw new BoardException(MISSING_NOTIFICATION_PROPERTY, placeholder);
                }

                properties.put(placeholder, property);
            }

            this.properties.put(notification, properties);
        }
    }

    public void sendNotification(NotificationRequest request) {
        User recipient = request.getRecipient();
        String recipientEmail = recipient.getEmail();
        Notification notification = request.getNotification();

        Map<String, String> properties = new HashMap<>();
        this.properties.get(notification)
            .forEach((key, value) -> properties.put(key, value.getValue(request)));

        StrSubstitutor parser = new StrSubstitutor(properties);
        String subject = parser.replace(this.subjects.get(notification));
        String content = parser.replace(this.contents.get(notification));

        if (recipientEmail.endsWith("@test.prism.hr")) {
            // Front end integration tests
            Long creatorId = recipient.getId() == null ? request.getResource().getCreatorId() : recipient.getId();
            testEmailService.createTestEmail(recipient, subject, makePlainTextVersion(content),
                request.getAttachments().stream().map(BoardAttachments::getUrl).collect(toList()), creatorId);
        } else if (mailOn) {
            // Real emails
            Mail mail = new Mail();
            mail.setFrom(new Email(senderEmail, "PRiSM Board"));

            Personalization personalization = new Personalization();
            personalization.addTo(new Email(recipientEmail));
            mail.addPersonalization(personalization);

            mail.setSubject(subject);
            mail.addContent(new Content("text/plain", makePlainTextVersion(content)));
            mail.addContent(new Content("text/html", content));
            request.getAttachments().forEach(mail::addAttachments);

            try {
                Request sendGridRequest = new Request();
                sendGridRequest.setMethod(POST);
                sendGridRequest.setEndpoint("mail/send");
                sendGridRequest.setBody(mail.build());
                sendGrid.api(sendGridRequest);

                LOGGER.info("Sending notification: " + makeLogHeader(notification, senderEmail, recipientEmail));
            } catch (IOException e) {
                LOGGER.error("Failed to send notification", e);
                throw new BoardException(UNDELIVERABLE_NOTIFICATION, "Could not deliver notification: " + notification);
            }
        } else {
            LOGGER.info("Sending notification: " + makeLogHeader(notification, senderEmail, recipientEmail)
                + "\n\n" + "Subject: " + subject + "\nContent:\n\n" + makePlainTextVersion(content) + "\n");
        }
    }

    private List<String> indexTemplate(Notification notification, String template, Map<Notification, String> index) {
        index.put(notification, template);
        return asList(StringUtils.substringsBetween(template, "${", "}"));
    }

    private String makeLogHeader(Notification notification, String sender, String recipient) {
        return "notification=" + notification + ", sender=" + sender + ", recipient=" + recipient;
    }

    private String makePlainTextVersion(String html) {
        Document document = Jsoup.parse(html);
        Element contentBody = document.getElementsByClass("comment_body_td").first();

        StringBuilder stringBuilder = new StringBuilder(EMPTY);
        for (Element element : contentBody.children()) {
            String tagName = element.tagName();
            if (tagName.equals("p")) {
                List<Element> as = element.select("a");
                if (as.isEmpty()) {
                    String[] lines = element.html().split("<br>");
                    stringBuilder.append(stream(lines).map(String::trim).collect(joining("\n")))
                        .append("\n\n");
                } else {
                    for (Element a : as) {
                        stringBuilder.append("\t- ").append(a.text()).append(": ")
                            .append(a.attr("abs:href")).append("\n");
                    }

                    stringBuilder.append("\n");
                }
            } else if (tagName.equals("ul")) {
                for (Element li : element.select("li")) {
                    stringBuilder.append("\t- ").append(li.text()).append("\n");
                }

                stringBuilder.append("\n");
            }
        }

        return stringBuilder.toString().replaceAll("\\n$", EMPTY);
    }

    private static String toString(org.springframework.core.io.Resource resource) throws IOException {
        try (InputStream inputStream = resource.getInputStream()) {
            String template = IOUtils.toString(inputStream, UTF_8).trim();
            return template.trim();
        }
    }

    public static class NotificationRequest {

        private Notification notification;

        private User recipient;

        private String invitation;

        private Resource resource;

        private Action action;

        private List<BoardAttachments> attachments = new ArrayList<>();

        public NotificationRequest(Notification notification, User recipient, String invitation, Resource resource,
                                   Action action, List<BoardAttachments> attachments) {
            this.notification = notification;
            this.recipient = recipient;
            this.invitation = invitation;
            this.resource = resource;
            this.action = action;
            this.attachments.addAll(attachments);
        }

        public Notification getNotification() {
            return notification;
        }

        public User getRecipient() {
            return recipient;
        }

        public String getInvitation() {
            return invitation;
        }

        public Resource getResource() {
            return resource;
        }

        public Action getAction() {
            return action;
        }

        @SuppressWarnings("WeakerAccess")
        public List<BoardAttachments> getAttachments() {
            return attachments;
        }

    }

}
