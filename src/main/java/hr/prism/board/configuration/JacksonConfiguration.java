package hr.prism.board.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import javax.inject.Inject;

import static hr.prism.board.utils.JacksonUtils.OBJECT_MAPPER;
import static hr.prism.board.utils.JacksonUtils.PRETTY_PRINT_OBJECT_MAPPER;

public class JacksonConfiguration {

    private final boolean jacksonPretty;

    @Inject
    public JacksonConfiguration(@Value("${jackson.pretty}") boolean jacksonPretty) {
        this.jacksonPretty = jacksonPretty;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return jacksonPretty ? PRETTY_PRINT_OBJECT_MAPPER : OBJECT_MAPPER;
    }

}
