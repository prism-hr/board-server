package hr.prism.board.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.core.JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

public class JacksonUtils {

    public static final ObjectMapper OBJECT_MAPPER = createObjectMapper();

    public static final ObjectMapper PRETTY_PRINT_OBJECT_MAPPER = createPrettyPrintObjectMapper();

    private static ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setSerializationInclusion(NON_NULL);
        objectMapper.enable(WRITE_BIGDECIMAL_AS_PLAIN);
        objectMapper.disable(WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.disable(FAIL_ON_UNKNOWN_PROPERTIES);
        return objectMapper;
    }

    private static ObjectMapper createPrettyPrintObjectMapper() {
        return createObjectMapper().enable(INDENT_OUTPUT);
    }

}
