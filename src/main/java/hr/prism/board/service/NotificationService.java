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
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StrSubstitutor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
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

    private MailStrategy mailStrategy;

    private Map<Notification, String> subjects = new TreeMap<>();

    private Map<Notification, String> contents = new TreeMap<>();

    private Map<Notification, Map<String, NotificationProperty>> properties = new TreeMap<>();

    @Inject
    private SendGrid sendGrid;

    @Inject
    private ApplicationContext applicationContext;

    @Inject
    private Environment environment;

    @Value("${system.email}")
    private String senderEmail;

    @PostConstruct
    public void postConstruct() throws IOException {
        String[] activeProfiles = environment.getActiveProfiles();
        if (ArrayUtils.contains(activeProfiles, "local") || ArrayUtils.contains(activeProfiles, "test")) {
            mailStrategy = MailStrategy.LOG;
        } else {
            mailStrategy = MailStrategy.SEND;
        }

        Map<String, NotificationProperty> propertiesMap =
            applicationContext.getBeansOfType(NotificationProperty.class).values().stream().collect(Collectors.toMap(NotificationProperty::getKey, property -> property));

        for (Notification notification : Notification.values()) {
            org.springframework.core.io.Resource subject = applicationContext.getResource("classpath:notification/subject/" + notification + ".html");
            org.springframework.core.io.Resource content = applicationContext.getResource("classpath:notification/content/" + notification + ".html");
            if (!subject.exists() || !content.exists()) {
                throw new BoardException(ExceptionCode.MISSING_NOTIFICATION);
            }

            Set<String> placeholders = new HashSet<>();
            placeholders.addAll(indexTemplate(notification, subject, this.subjects));
            placeholders.addAll(indexTemplate(notification, content, this.contents));

            Map<String, NotificationProperty> properties = new TreeMap<>();
            for (String placeholder : placeholders) {
                NotificationProperty property = propertiesMap.get(placeholder);
                if (property == null) {
                    throw new BoardException(ExceptionCode.MISSING_NOTIFICATION_PROPERTY);
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

        if (mailStrategy == MailStrategy.LOG) {
            // Local/Test contexts
            LOGGER.info("Sending notification: " + makeLogHeader(notification, senderEmail, recipientEmail)
                + "\n\n" + "Subject:\n\n" + subject + "\n\n" + "Content:\n\n" + makePlainTextVersion(content));
        } else {
            // Production/UAT contexts
            Mail mail = new Mail();
            mail.setFrom(new Email(senderEmail));

            Personalization personalization = new Personalization();
            personalization.addTo(new Email(recipientEmail));
            mail.addPersonalization(personalization);

            mail.setSubject(subject);
            mail.addContent(new Content("text/plain", makePlainTextVersion(content)));
            mail.addContent(new Content("text/html", content));

            try {
                Request sendGridRequest = new Request();
                sendGridRequest.setMethod(Method.POST);
                sendGridRequest.setEndpoint("mail/send");
                sendGridRequest.setBody(mail.build());
                sendGrid.api(sendGridRequest);

                LOGGER.info("Sending notification: " + makeLogHeader(notification, senderEmail, recipientEmail));
            } catch (IOException e) {
                LOGGER.error("Failed to send notification", e);
                throw new BoardException(ExceptionCode.UNDELIVERABLE_NOTIFICATION);
            }
        }

        return properties;
    }

    private List<String> indexTemplate(Notification notification, org.springframework.core.io.Resource resource, Map<Notification, String> index) throws IOException {
        InputStream inputStream = resource.getInputStream();
        String template = IOUtils.toString(inputStream, StandardCharsets.UTF_8).trim();
        index.put(notification, template);
        IOUtils.closeQuietly(inputStream);
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

    private enum MailStrategy {
        LOG, SEND
    }

    public static class NotificationRequest {

        private Notification notification;

        private User recipient;

        private Resource resource;

        private Action action;

        private Map<String, String> customProperties;

        public NotificationRequest(Notification notification, User recipient, Resource resource, Action action) {
            this.notification = notification;
            this.recipient = recipient;
            this.resource = resource;
            this.action = action;
        }

        public Notification getNotification() {
            return notification;
        }

        public User getRecipient() {
            return recipient;
        }

        public Resource getResource() {
            return resource;
        }

        public Action getAction() {
            return action;
        }

    }

}
