package hr.prism.board.repository;

import hr.prism.board.domain.Search;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@NoRepositoryBean
public interface SearchRepository<T extends Search> extends BoardEntityRepository<T, Long> {

    void deleteBySearch(String search);

}
