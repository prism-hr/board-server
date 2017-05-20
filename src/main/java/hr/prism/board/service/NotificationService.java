package hr.prism.board.service;

import com.google.common.collect.Maps;
import com.sendgrid.*;
import hr.prism.board.domain.User;
import hr.prism.board.exception.ApiException;
import hr.prism.board.exception.ExceptionCode;
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
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class NotificationService {
    
    private static Logger LOGGER = LoggerFactory.getLogger(NotificationService.class);
    
    private MailStrategy mailStrategy;
    
    private Map<String, String> subjects;
    
    private Map<String, String> contents;
    
    @Inject
    private Environment environment;
    
    @Inject
    private SendGrid sendGrid;
    
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
        
        Resource[] subjects = applicationContext.getResources("classpath:notification/subject/*.html");
        Resource[] contents = applicationContext.getResources("classpath:notification/content/*.html");
    
        this.subjects = indexResources(subjects);
        this.contents = indexResources(contents);
        Assert.isTrue(this.subjects.keySet().equals(this.contents.keySet()), "Every template must have a subject and content");
    }
    
    public Notification makeNotification(String template, User recipient, Map<String, String> customParameters) {
        String recipientEmail = recipient.getEmail();
        String senderEmail = environment.getProperty("system.email");
        Map<String, String> parameters = Maps.newLinkedHashMap(customParameters);
        parameters.put("firstName", recipient.getGivenName());
        return new Notification(template, senderEmail, recipientEmail, parameters);
    }
    
    public void send(Notification notification) {
        String template = notification.getTemplate();
        String sender = notification.getSender();
        String recipient = notification.getRecipient();
        Map<String, String> parameters = notification.getParameters();
        
        StrSubstitutor parser = new StrSubstitutor(parameters);
        String subject = parser.replace(this.subjects.get(template));
        String content = parser.replace(this.contents.get(template));
        
        if (mailStrategy == MailStrategy.LOG) {
            // Local/Test contexts
            LOGGER.info("Sending notification: " + makeLogHeader(template, sender, recipient)
                + "\n\n" + "Subject:\n\n" + subject + "\n\n" + "Content:\n\n" + makePlainTextVersion(content));
        } else {
            // Production/UAT contexts
            Mail mail = new Mail();
            mail.setFrom(new Email(sender));
            
            Personalization personalization = new Personalization();
            personalization.addTo(new Email(recipient));
            mail.addPersonalization(personalization);
            
            mail.setSubject(subject);
            mail.addContent(new Content("text/plain", makePlainTextVersion(content)));
            mail.addContent(new Content("text/html", content));
            
            try {
                Request request = new Request();
                request.method = Method.POST;
                request.endpoint = "mail/send";
                request.body = mail.build();
                sendGrid.api(request);
    
                LOGGER.info("Sending notification: " + makeLogHeader(template, sender, recipient));
            } catch (IOException e) {
                LOGGER.error("Failed to send notification", e);
                throw new ApiException(ExceptionCode.UNDELIVERABLE_NOTIFICATION);
            }
        }
    }
    
    private String makeLogHeader(String notification, String sender, String recipient) {
        return "notification=" + notification + ", sender=" + sender + ", recipient=" + recipient;
    }
    
    // FIXME: remove trailing line break
    private String makePlainTextVersion(String html) {
        Document document = Jsoup.parse(html);
        Element body = document.select("body").first();
        
        String plainText = StringUtils.EMPTY;
        for (Element p : body.select("p")) {
            List<Element> as = p.select("a");
            if (CollectionUtils.isEmpty(as)) {
                String[] lines = p.html().split("<br>");
                plainText = plainText + Arrays.stream(lines).map(String::trim).collect(Collectors.joining("\n")) + "\n\n";
            } else {
                for (Element a : p.select("a")) {
                    plainText = plainText + "\t- " + a.text() + ": " + a.attr("abs:href") + "\n";
                }
                plainText = plainText + "\n";
            }
        }
        
        return plainText;
    }
    
    private static Map<String, String> indexResources(Resource[] resources) throws IOException {
        TreeMap<String, String> index = new TreeMap<>();
        for (Resource resource : resources) {
            String fileName = resource.getFilename();
            index.put(fileName.replace(".html", ""), IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8).trim());
        }
        
        return index;
    }
    
    private enum MailStrategy {
        LOG, SEND
    }
    
    public static class Notification {
        
        private String template;
        
        private String sender;
        
        private String recipient;
        
        private Map<String, String> parameters;
        
        public Notification(String template, String sender, String recipient, Map<String, String> parameters) {
            this.template = template;
            this.sender = sender;
            this.recipient = recipient;
            this.parameters = parameters;
        }
        
        public String getTemplate() {
            return template;
        }
        
        public String getSender() {
            return sender;
        }
        
        public String getRecipient() {
            return recipient;
        }
        
        public Map<String, String> getParameters() {
            return parameters;
        }
        
    }
    
}
