package hr.prism.board.configuration;

import freemarker.template.TemplateException;
import no.api.freemarker.java8.Java8ObjectWrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.IOException;

import static freemarker.template.Configuration.VERSION_2_3_26;

public class FreeMarkerConfiguration {

    @Bean
    public FreeMarkerConfigurer freemarkerConfig() {
        FreeMarkerConfigurer freeMarkerConfigurer = new CustomFreeMarkerConfigurer();
        freeMarkerConfigurer.setTemplateLoaderPaths("classpath:badge", "classpath:index");
        return freeMarkerConfigurer;
    }

    private class CustomFreeMarkerConfigurer extends FreeMarkerConfigurer {

        @Override
        public void afterPropertiesSet() throws IOException, TemplateException {
            super.afterPropertiesSet();
            this.getConfiguration()
                .setObjectWrapper(new Java8ObjectWrapper(VERSION_2_3_26));
        }

    }

}
