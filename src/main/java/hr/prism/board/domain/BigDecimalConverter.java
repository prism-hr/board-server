package hr.prism.board.domain;

import hr.prism.board.exception.ApiException;
import hr.prism.board.exception.ExceptionCode;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;

@Converter(autoApply = true)
public class BigDecimalConverter implements AttributeConverter<BigDecimal, String> {
    
    @Override
    public String convertToDatabaseColumn(BigDecimal attribute) {
        return attribute == null ? null : attribute.stripTrailingZeros().toPlainString();
    }
    
    @Override
    public BigDecimal convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        
        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.setParseBigDecimal(true);
        
        try {
            return ((BigDecimal) decimalFormat.parse(dbData)).stripTrailingZeros();
        } catch (ParseException e) {
            throw new ApiException(ExceptionCode.PROBLEM);
        }
    }
    
}
