package hr.prism.board.repository;

import hr.prism.board.domain.BoardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

@NoRepositoryBean
public interface BoardEntityRepository<ENTITY extends BoardEntity, ID extends Serializable>
    extends JpaRepository<ENTITY, ID> {

    <T extends ENTITY> T save(T entity);

    <T extends ENTITY> T update(T entity);

    <T extends ENTITY> T updateSilently(T entity);

}
