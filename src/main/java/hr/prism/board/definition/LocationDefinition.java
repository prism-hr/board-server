package hr.prism.board.definition;

import java.math.BigDecimal;

public interface LocationDefinition {
    
    public String getName();
    
    public String getDomicile();
    
    public String getGoogleId();
    
    public BigDecimal getLatitude();
    
    public BigDecimal getLongitude();
    
}
