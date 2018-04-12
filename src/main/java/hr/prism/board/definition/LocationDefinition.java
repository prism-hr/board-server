package hr.prism.board.definition;

import java.math.BigDecimal;

public interface LocationDefinition {

    String getName();

    String getDomicile();

    String getGoogleId();

    BigDecimal getLatitude();

    BigDecimal getLongitude();

}
