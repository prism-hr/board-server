package hr.prism.board.domain.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

import static java.util.Locale.UK;

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

        NumberFormat numberFormat = NumberFormat.getNumberInstance(UK);
        DecimalFormat decimalFormat = (DecimalFormat) numberFormat;
        decimalFormat.setParseBigDecimal(true);

        try {
            return ((BigDecimal) decimalFormat.parse(dbData)).stripTrailingZeros();
        } catch (ParseException e) {
            throw new Error(e);
        }
    }

}
