package hr.prism.board.value;

public class DocumentSearch {

    private String documentCloudinaryId;

    private String documentCloudinaryUrl;

    private String documentFileName;

    public DocumentSearch(String documentCloudinaryId, String documentCloudinaryUrl, String documentFileName) {
        this.documentCloudinaryId = documentCloudinaryId;
        this.documentCloudinaryUrl = documentCloudinaryUrl;
        this.documentFileName = documentFileName;
    }

    public String getDocumentCloudinaryId() {
        return documentCloudinaryId;
    }

    public String getDocumentCloudinaryUrl() {
        return documentCloudinaryUrl;
    }

    public String getDocumentFileName() {
        return documentFileName;
    }

}
