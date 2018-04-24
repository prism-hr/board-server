package hr.prism.board.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.TemplateException;
import no.api.freemarker.java8.Java8ObjectWrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

import static freemarker.template.Configuration.VERSION_2_3_26;

@Configuration
public class WebMvcConfiguration extends WebMvcConfigurerAdapter {

    private final ObjectMapper objectMapper;

    @Inject
    public WebMvcConfiguration(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    public FreeMarkerConfigurer freemarkerConfig() {
        FreeMarkerConfigurer freeMarkerConfigurer = new CustomFreeMarkerConfigurer();
        freeMarkerConfigurer.setTemplateLoaderPaths("classpath:badge", "classpath:index");
        return freeMarkerConfigurer;
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new MappingJackson2HttpMessageConverter(objectMapper));
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
