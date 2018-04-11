package hr.prism.board.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.TemplateException;
import hr.prism.board.utils.ObjectMapperProvider;
import no.api.freemarker.java8.Java8ObjectWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;
import static freemarker.template.Configuration.VERSION_2_3_26;

@EnableWebMvc
@Configuration
public class WebMvcConfiguration extends WebMvcConfigurerAdapter {

    private final boolean jacksonPretty;

    @Inject
    public WebMvcConfiguration(@Value("${jackson.pretty}") boolean jacksonPretty) {
        this.jacksonPretty = jacksonPretty;
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = ObjectMapperProvider.getObjectMapper().copy();
        if (jacksonPretty) {
            objectMapper.enable(INDENT_OUTPUT);
        }

        return objectMapper;
    }

    @Bean
    public FreeMarkerConfigurer freemarkerConfig() {
        FreeMarkerConfigurer freeMarkerConfigurer = new CustomFreeMarkerConfigurer();
        freeMarkerConfigurer.setTemplateLoaderPaths("classpath:badge", "classpath:index");
        return freeMarkerConfigurer;
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new MappingJackson2HttpMessageConverter(objectMapper()));
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
