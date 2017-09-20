package hr.prism.board.service;

import com.sendgrid.*;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.Notification;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.notification.property.NotificationProperty;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StrSubstitutor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private static Logger LOGGER = LoggerFactory.getLogger(NotificationService.class);

    private Map<Notification, String> subjects = new TreeMap<>();

    private Map<Notification, String> contents = new TreeMap<>();

    private Map<Notification, Map<String, NotificationProperty>> properties = new TreeMap<>();

    @Value("${mail.on}")
    private Boolean mailOn;

    @Value("${system.email}")
    private String senderEmail;

    @Inject
    private SendGrid sendGrid;

    @Inject
    private ApplicationContext applicationContext;

    @PostConstruct
    public void postConstruct() throws IOException {
        Map<String, NotificationProperty> propertiesMap =
            applicationContext.getBeansOfType(NotificationProperty.class).values().stream().collect(Collectors.toMap(NotificationProperty::getKey, property -> property));

        String header = toString(applicationContext.getResource("classpath:notification/content/header.html"));
        String footer = toString(applicationContext.getResource("classpath:notification/content/footer.html"));

        for (Notification notification : Notification.values()) {
            String subject = toString(applicationContext.getResource("classpath:notification/subject/" + notification + ".html"));
            String content = toString(applicationContext.getResource("classpath:notification/content/" + notification + ".html"))
                .replace("${header}", header).replace("${footer}", footer);

            Set<String> placeholders = new HashSet<>();
            placeholders.addAll(indexTemplate(notification, subject, this.subjects));
            placeholders.addAll(indexTemplate(notification, content, this.contents));

            Map<String, NotificationProperty> properties = new TreeMap<>();
            for (String placeholder : placeholders) {
                NotificationProperty property = propertiesMap.get(placeholder);
                if (property == null) {
                    throw new BoardException(ExceptionCode.MISSING_NOTIFICATION_PROPERTY, placeholder);
                }

                properties.put(placeholder, property);
            }

            this.properties.put(notification, properties);
        }
    }

    public Map<String, String> sendNotification(NotificationRequest request) {
        String recipientEmail = request.getRecipient().getEmail();
        Notification notification = request.getNotification();

        Map<String, String> properties = this.properties.get(notification).entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getValue(request)));

        StrSubstitutor parser = new StrSubstitutor(properties);
        String subject = parser.replace(this.subjects.get(notification));
        String content = parser.replace(this.contents.get(notification));

        if (BooleanUtils.isTrue(mailOn)) {
            // Production/UAT contexts
            Mail mail = new Mail();
            mail.setFrom(new Email(senderEmail));

            Personalization personalization = new Personalization();
            personalization.addTo(new Email(recipientEmail));
            mail.addPersonalization(personalization);

            mail.setSubject(subject);
            mail.addContent(new Content("text/plain", makePlainTextVersion(content)));
            mail.addContent(new Content("text/html", content));
            request.getAttachments().forEach(mail::addAttachments);

            try {
                Request sendGridRequest = new Request();
                sendGridRequest.setMethod(Method.POST);
                sendGridRequest.setEndpoint("mail/send");
                sendGridRequest.setBody(mail.build());
                sendGrid.api(sendGridRequest);

                LOGGER.info("Sending notification: " + makeLogHeader(notification, senderEmail, recipientEmail));
            } catch (IOException e) {
                LOGGER.error("Failed to send notification", e);
                throw new BoardException(ExceptionCode.UNDELIVERABLE_NOTIFICATION, "Could not deliver notification: " + notification);
            }
        } else {
            // Local/Test contexts
            LOGGER.info("Sending notification: " + makeLogHeader(notification, senderEmail, recipientEmail)
                + "\n\n" + "Subject:\n\n" + subject + "\n\nHTML Content:\n\n" + content + "\n\nText Content:\n\n" + makePlainTextVersion(content) + "\n");
        }

        return properties;
    }

    private List<String> indexTemplate(Notification notification, String template, Map<Notification, String> index) throws IOException {
        index.put(notification, template);
        return Arrays.asList(StringUtils.substringsBetween(template, "${", "}"));
    }

    private String makeLogHeader(Notification notification, String sender, String recipient) {
        return "notification=" + notification + ", sender=" + sender + ", recipient=" + recipient;
    }

    private String makePlainTextVersion(String html) {
        Document document = Jsoup.parse(html);
        Element body = document.select("body").first();

        String plainText = StringUtils.EMPTY;
        for (Element p : body.select("p")) {
            List<Element> as = p.select("a");
            if (CollectionUtils.isEmpty(as)) {
                String[] lines = p.html().split("<br>");
                plainText += Arrays.stream(lines).map(String::trim).collect(Collectors.joining("\n")) + "\n\n";
            } else {
                for (Element a : p.select("a")) {
                    plainText += "\t- " + a.text() + ": " + a.attr("abs:href") + "\n";
                }

                plainText += "\n";
            }
        }

        return plainText.replaceAll("\\n$", StringUtils.EMPTY);
    }

    private static String toString(org.springframework.core.io.Resource resource) throws IOException {
        InputStream inputStream = resource.getInputStream();
        String template = IOUtils.toString(inputStream, StandardCharsets.UTF_8).trim();
        IOUtils.closeQuietly(inputStream);
        return template.trim();
    }

    public static class NotificationRequest {

        private Notification notification;

        private User recipient;

        private String invitation;

        private Resource resource;

        private Action action;

        private List<Attachments> attachments = new ArrayList<>();

        public NotificationRequest(Notification notification, User recipient, String invitation, Resource resource, Action action, List<Attachments> attachments) {
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

        public List<Attachments> getAttachments() {
            return attachments;
        }

    }

}
