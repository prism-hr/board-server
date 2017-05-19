package hr.prism.board.service;

import com.google.common.base.MoreObjects;
import com.sendgrid.*;
import hr.prism.board.domain.User;
import hr.prism.board.exception.ApiException;
import hr.prism.board.exception.ExceptionCode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NotificationService {
    
    Logger LOGGER = LoggerFactory.getLogger(NotificationService.class);
    
    @Inject
    private Environment environment;
    
    @Inject
    private SendGrid sendGrid;
    
    @Inject
    private TemplateEngine templateEngine;
    
    public Pair<String, String> send(User user, String notification, Map<String, String> parameters) {
        Context context = new Context();
        context.setVariable("firstName", user.getGivenName());
        parameters.keySet().forEach(key -> context.setVariable(key, parameters.get(key)));
        
        String sender = environment.getProperty("system.email");
        String recipient = user.getEmail();
        
        String subject = templateEngine.process("subject/" + notification, context);
        String content = templateEngine.process("content/" + notification, context);
        
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
                
                LOGGER.info("Sending notification:\n" + makeLogHeader(notification, sender, recipient));
            } catch (IOException ex) {
                throw new ApiException(ExceptionCode.UNDELIVERABLE_NOTIFICATION);
            }
        } else {
            // Local/Test contexts
            LOGGER.info("Sending notification:\n" + makeLogHeader(notification, sender, recipient)
                + "\n\n" + "Subject:\n" + subject + "\n\n" + "Content:\n" + makePlainTextVersion(content));
        }
        
        return Pair.of(subject, content);
    }
    
    private String makeLogHeader(String notification, String sender, String recipient) {
        return MoreObjects.toStringHelper(this).add("notification", notification).add("sender", sender).add("recipient", recipient).toString();
    }
    
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
    
}
