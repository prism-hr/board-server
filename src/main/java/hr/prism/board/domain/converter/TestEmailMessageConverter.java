package hr.prism.board.domain.converter;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import hr.prism.board.configuration.WebMvcConfiguration;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.representation.TestEmailMessageRepresentation;

@SuppressWarnings("unused")
@Converter(autoApply = true)
public class TestEmailMessageConverter implements AttributeConverter<TestEmailMessageRepresentation, String> {

    @Override
    public String convertToDatabaseColumn(TestEmailMessageRepresentation attribute) {
        try {
            return attribute == null ? null : WebMvcConfiguration.OBJECT_MAPPER.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new BoardException(ExceptionCode.PROBLEM, "Could not serialize test email", e);
        }
    }

    @Override
    public TestEmailMessageRepresentation convertToEntityAttribute(String dbData) {
        try {
            return dbData == null ? null : WebMvcConfiguration.OBJECT_MAPPER.readValue(dbData, TestEmailMessageRepresentation.class);
        } catch (IOException e) {
            throw new BoardException(ExceptionCode.PROBLEM, "Could not deserialize test email", e);
        }
    }

}
