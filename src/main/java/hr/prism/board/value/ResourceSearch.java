package hr.prism.board.value;

public class ResourceSearch extends DocumentSearch {

    private final Long id;

    private final String name;

    public ResourceSearch(Long id, String name, String documentCloudinaryId, String documentCloudinaryUrl,
                          String documentFileName) {
        super(documentCloudinaryId, documentCloudinaryUrl, documentFileName);
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}
