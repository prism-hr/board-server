package hr.prism.board.service;

import com.google.common.collect.Maps;
import com.sendgrid.*;
import hr.prism.board.domain.User;
import hr.prism.board.exception.ApiException;
import hr.prism.board.exception.ExceptionCode;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.commons.lang3.tuple.Pair;
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
        Resource[] subjects = applicationContext.getResources("classpath:notification/subject/*.html");
        Resource[] contents = applicationContext.getResources("classpath:notification/content/*.html");
        
        this.subjects = indexResources(subjects);
        this.contents = indexResources(contents);
        Assert.isTrue(this.subjects.keySet().equals(this.contents.keySet()), "Every template must have a subject and content");
    }
    
    public Pair<String, String> send(User user, String notification, Map<String, String> customParameters) {
        String sender = environment.getProperty("system.email");
        String recipient = user.getEmail();
        
        Map<String, String> parameters = Maps.newHashMap(customParameters);
        parameters.put("firstName", user.getGivenName());
        
        StrSubstitutor parser = new StrSubstitutor(parameters);
        String subject = parser.replace(this.subjects.get(notification));
        String content = parser.replace(this.contents.get(notification));
        
        if (environment.getProperty("mail.strategy").equals("send")) {
            // Production/UAT contexts
            Mail mail = new Mail();
            mail.setFrom(new Email(sender));
            
            Personalization personalization = new Personalization();
            personalization.addTo(new Email(recipient));
            mail.addPersonalization(personalization);
            
            mail.setSubject(subject);
            mail.addContent(new Content("text/html", content));
            mail.addContent(new Content("text/plain", makePlainTextVersion(content)));
            
            try {
                Request request = new Request();
                request.method = Method.POST;
                request.endpoint = "mail/send";
                request.body = mail.build();
                sendGrid.api(request);
    
                LOGGER.info("Sending notification: " + makeLogHeader(notification, sender, recipient));
            } catch (IOException ex) {
                throw new ApiException(ExceptionCode.UNDELIVERABLE_NOTIFICATION);
            }
        } else {
            // Local/Test contexts
            LOGGER.info("Sending notification: " + makeLogHeader(notification, sender, recipient)
                + "\n\n" + "Subject:\n\n" + subject + "\n\n" + "Content:\n\n" + makePlainTextVersion(content));
        }
        
        return Pair.of(subject, content);
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
            index.put(fileName.replace(".html", ""), FileUtils.readFileToString(resource.getFile(), StandardCharsets.UTF_8).trim());
        }
        
        return index;
    }
    
}
