package hr.prism.board.repository;

import hr.prism.board.domain.BoardEntity;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.io.Serializable;

@NoRepositoryBean
public interface MyRepository<ENTITY extends BoardEntity, ID extends Serializable>
    extends PagingAndSortingRepository<ENTITY, ID> {
    
    <T extends ENTITY> void update(T entity);
    
}
