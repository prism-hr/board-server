package hr.prism.board.service;

import hr.prism.board.domain.Board;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

import static hr.prism.board.enums.CategoryType.POST;

@Service
@Transactional
public class BoardPatchService extends ResourcePatchService<Board> {

    @Inject
    public BoardPatchService(LocationService locationService, DocumentService documentService,
                             ResourceService resourceService) {
        super(locationService, documentService, resourceService);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public void patchPostCategories(Board board, Optional<List<String>> categories) {
        patchCategories(board, POST, categories);
    }

}
