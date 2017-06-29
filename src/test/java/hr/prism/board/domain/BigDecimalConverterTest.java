package hr.prism.board.domain;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

public class BigDecimalConverterTest {

    private BigDecimalConverter converter = new BigDecimalConverter();

    @Test
    public void shouldConvertToEntityAttribute() {
        BigDecimal bigDecimal = converter.convertToEntityAttribute("1.00000000000000");
        Assert.assertEquals(BigDecimal.ONE, bigDecimal);
    }

}
