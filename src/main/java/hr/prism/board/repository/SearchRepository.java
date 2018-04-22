package hr.prism.board.repository;

import hr.prism.board.domain.Search;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@NoRepositoryBean
public interface SearchRepository<T extends Search> extends JpaRepository<T, Long> {

    void deleteBySearch(String search);

}
