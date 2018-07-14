package hr.prism.board.enums;

public enum AgeRange {

    ZERO_EIGHTEEN("0-18"),
    NINETEEN_TWENTYFOUR("19-24"),
    TWENTYFIVE_TWENTYNINE("25-29"),
    THIRTY_THIRTYNINE("30-39"),
    FORTY_FORTYNINE("40-49"),
    FIFTY_SIXTYFOUR("50-64"),
    SIXTYFIVE_PLUS("65+");

    private String shortName;

    AgeRange(String shortName) {
        this.shortName = shortName;
    }

    @Override
    public String toString() {
        return shortName;
    }

}
