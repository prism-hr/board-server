package hr.prism.board.value;

public class DepartmentSearch {

    private final Long id;

    private final String name;

    private final String documentLogoCloudinaryId;

    private final String documentLogoCloudinaryUrl;

    private final String documentLogoFileName;

    public DepartmentSearch(Long id, String name, String documentLogoCloudinaryId, String documentLogoCloudinaryUrl,
                            String documentLogoFileName) {
        this.id = id;
        this.name = name;
        this.documentLogoCloudinaryId = documentLogoCloudinaryId;
        this.documentLogoCloudinaryUrl = documentLogoCloudinaryUrl;
        this.documentLogoFileName = documentLogoFileName;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDocumentLogoCloudinaryId() {
        return documentLogoCloudinaryId;
    }

    public String getDocumentLogoCloudinaryUrl() {
        return documentLogoCloudinaryUrl;
    }

    public String getDocumentLogoFileName() {
        return documentLogoFileName;
    }

}
