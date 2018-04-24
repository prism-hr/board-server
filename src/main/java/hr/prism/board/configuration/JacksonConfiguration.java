package hr.prism.board.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import javax.inject.Inject;

import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;
import static hr.prism.board.utils.JacksonUtils.getObjectMapper;

public class JacksonConfiguration {

    private final boolean jacksonPretty;

    @Inject
    public JacksonConfiguration(@Value("${jackson.pretty}") boolean jacksonPretty) {
        this.jacksonPretty = jacksonPretty;
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = getObjectMapper().copy();
        if (jacksonPretty) {
            objectMapper.enable(INDENT_OUTPUT);
        }

        return objectMapper;
    }

}
