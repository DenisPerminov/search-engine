package searchengine.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Indexes;

@Repository
public interface IndexRepository extends CrudRepository<Indexes, Integer> {
}
