package hr.prism.board.domain.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import hr.prism.board.exception.BoardException;
import hr.prism.board.representation.TestEmailMessageRepresentation;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.IOException;

import static hr.prism.board.exception.ExceptionCode.UNKNOWN;
import static hr.prism.board.utils.ObjectMapperUtils.getObjectMapper;

@SuppressWarnings("unused")
@Converter(autoApply = true)
public class TestEmailMessageConverter implements AttributeConverter<TestEmailMessageRepresentation, String> {

    @Override
    public String convertToDatabaseColumn(TestEmailMessageRepresentation attribute) {
        try {
            return attribute == null ? null : getObjectMapper().writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new BoardException(UNKNOWN, "Could not serialize test email", e);
        }
    }

    @Override
    public TestEmailMessageRepresentation convertToEntityAttribute(String dbData) {
        try {
            return dbData == null ? null : getObjectMapper().readValue(dbData, TestEmailMessageRepresentation.class);
        } catch (IOException e) {
            throw new BoardException(UNKNOWN, "Could not deserialize test email", e);
        }
    }

}
