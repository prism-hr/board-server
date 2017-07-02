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
import org.apache.commons.lang3.text.StrSubstitutor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

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

    private Map<String, NotificationTemplate> subjects;

    private Map<String, NotificationTemplate> contents;

    @Inject
    private SendGrid sendGrid;

    @Inject
    private Environment environment;

    @Inject
    private ApplicationContext applicationContext;

    @PostConstruct
    public void postConstruct() throws IOException {
        String[] activeProfiles = environment.getActiveProfiles();
        if (ArrayUtils.contains(activeProfiles, "local") || ArrayUtils.contains(activeProfiles, "test")) {
            mailStrategy = MailStrategy.LOG;
        } else {
            mailStrategy = MailStrategy.SEND;
        }

        org.springframework.core.io.Resource[] subjects = applicationContext.getResources("classpath:notification/subject/*.html");
        org.springframework.core.io.Resource[] contents = applicationContext.getResources("classpath:notification/content/*.html");

        Map<String, NotificationProperty> propertiesMap =
            applicationContext.getBeansOfType(NotificationProperty.class).values().stream().collect(Collectors.toMap(NotificationProperty::getKey, property -> property));

        this.subjects = indexTemplates(subjects, propertiesMap);
        this.contents = indexTemplates(contents, propertiesMap);
        Assert.isTrue(this.subjects.keySet().equals(this.contents.keySet()), "Every template must have a subject and content");
    }

    public void sendNotification(NotificationInstance notificationInstance) {
        String senderEmail = environment.getProperty("system.email");
        String recipientEmail = notificationInstance.getRecipient().getEmail();
        String notification = notificationInstance.getNotification().toString();

        String subject = parseTemplate(this.subjects.get(notification), notificationInstance);
        String content = parseTemplate(this.contents.get(notification), notificationInstance);

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
                Request request = new Request();
                request.setMethod(Method.POST);
                request.setEndpoint("mail/send");
                request.setBody(mail.build());
                sendGrid.api(request);

                LOGGER.info("Sending notification: " + makeLogHeader(notification, senderEmail, recipientEmail));
            } catch (IOException e) {
                LOGGER.error("Failed to send notification", e);
                throw new BoardException(ExceptionCode.UNDELIVERABLE_NOTIFICATION);
            }
        }
    }

    private static Map<String, NotificationTemplate> indexTemplates(org.springframework.core.io.Resource[] resources, Map<String, NotificationProperty> propertiesMap) throws IOException {
        TreeMap<String, NotificationTemplate> index = new TreeMap<>();
        for (org.springframework.core.io.Resource resource : resources) {
            String fileName = resource.getFilename().replace(".html", "");
            Assert.notNull(Notification.valueOf(fileName), "Every template must have a related definition");
            InputStream inputStream = resource.getInputStream();

            Map<String, NotificationProperty> properties = new LinkedHashMap<>();
            String template = IOUtils.toString(inputStream, StandardCharsets.UTF_8).trim();
            String[] placeholders = StringUtils.substringsBetween(template, "${", "}");
            for (String placeholder : placeholders) {
                properties.put(placeholder, propertiesMap.get(placeholder));
            }

            Assert.isTrue(properties.size() == placeholders.length, "Every placeholder must have a property accessor");
            index.put(fileName, new NotificationTemplate(template, properties));
            IOUtils.closeQuietly(inputStream);
        }

        return index;
    }

    private String parseTemplate(NotificationTemplate notificationTemplate, NotificationInstance notificationInstance) {
        Map<String, String> properties = new LinkedHashMap<>();
        Map<String, NotificationProperty> subjectProperties = notificationTemplate.getProperties();
        for (String placeholder : subjectProperties.keySet()) {
            properties.put(placeholder, subjectProperties.get(placeholder).getValue(notificationInstance));
        }

        properties.putAll(notificationInstance.getCustomProperties());
        StrSubstitutor subjectParser = new StrSubstitutor(properties);
        return subjectParser.replace(notificationTemplate.getTemplate());
    }

    private String makeLogHeader(String notification, String sender, String recipient) {
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

                plainText += "\\n";
            }
        }

        return plainText.replaceAll("\n$", StringUtils.EMPTY);
    }

    private enum MailStrategy {
        LOG, SEND
    }

    public static class NotificationTemplate {

        private String template;

        private Map<String, NotificationProperty> properties;

        public NotificationTemplate(String template, Map<String, NotificationProperty> properties) {
            this.template = template;
            this.properties = properties;
        }

        public String getTemplate() {
            return template;
        }

        public Map<String, NotificationProperty> getProperties() {
            return properties;
        }

        @Override
        public String toString() {
            return template;
        }
    }

    public static class NotificationInstance {

        private Notification notification;

        private User recipient;

        private Resource resource;

        private Action action;

        private Map<String, String> customProperties;

        public NotificationInstance(Notification notification, User recipient, Resource resource, Action action, Map<String, String> customProperties) {
            this.notification = notification;
            this.recipient = recipient;
            this.resource = resource;
            this.action = action;
            this.customProperties = customProperties;
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

        public Map<String, String> getCustomProperties() {
            return customProperties;
        }

    }

}
