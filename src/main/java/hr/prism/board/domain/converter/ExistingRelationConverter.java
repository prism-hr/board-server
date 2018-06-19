package hr.prism.board.domain.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import hr.prism.board.exception.BoardException;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.IOException;
import java.util.Map;

import static hr.prism.board.exception.ExceptionCode.UNKNOWN;
import static hr.prism.board.utils.JacksonUtils.PRETTY_PRINT_OBJECT_MAPPER;

@Converter
public class ExistingRelationConverter implements AttributeConverter<Map<String, Object>, String> {

    @Override
    public String convertToDatabaseColumn(Map<String, Object> attribute) {
        try {
            return attribute == null ? null : PRETTY_PRINT_OBJECT_MAPPER.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new BoardException(UNKNOWN, "Unable to serialize existing relation explanation", e);
        }
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(String dbData) {
        try {
            return dbData == null ?
                null : PRETTY_PRINT_OBJECT_MAPPER.readValue(dbData, new TypeReference<Map<String, Object>>() {
            });
        } catch (IOException e) {
            throw new BoardException(UNKNOWN, "Unable to deserialize existing relation explanation", e);
        }
    }

}
