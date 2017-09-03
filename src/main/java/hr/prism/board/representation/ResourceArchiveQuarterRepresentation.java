package hr.prism.board.representation;

public class ResourceArchiveQuarterRepresentation {

    private Integer year;

    private Integer quarter;

    public static ResourceArchiveQuarterRepresentation fromString(String string) {
        return new ResourceArchiveQuarterRepresentation().setYear(Integer.parseInt(string.substring(0, 4))).setQuarter(Integer.parseInt(string.substring(4)));
    }

    public Integer getYear() {
        return year;
    }

    public ResourceArchiveQuarterRepresentation setYear(Integer year) {
        this.year = year;
        return this;
    }

    public Integer getQuarter() {
        return quarter;
    }

    public ResourceArchiveQuarterRepresentation setQuarter(Integer quarter) {
        this.quarter = quarter;
        return this;
    }

}
