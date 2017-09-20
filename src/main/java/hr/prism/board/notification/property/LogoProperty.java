package hr.prism.board.notification.property;

import hr.prism.board.domain.Board;
import hr.prism.board.domain.Document;
import hr.prism.board.domain.Post;
import hr.prism.board.domain.Resource;
import hr.prism.board.service.NotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class LogoProperty implements NotificationProperty {

    @Value("${prism.logo.url}")
    private String prismLogoUrl;

    @Inject
    private GlobalLogoProperty globalLogoProperty;

    @Override
    public String getValue(NotificationService.NotificationRequest notificationRequest) {
        Resource resource = notificationRequest.getResource();
        if (resource == null) {
            return globalLogoProperty.getValue(notificationRequest);
        }

        if (resource instanceof Post) {
            resource = resource.getParent();
        }

        Document documentLogo = resource.getDocumentLogo();
        if (documentLogo == null && resource instanceof Board) {
            documentLogo = resource.getParent().getDocumentLogo();
        }

        if (documentLogo == null) {
            return globalLogoProperty.getValue(notificationRequest);
        }

        return documentLogo.getCloudinaryUrl();
    }

}
