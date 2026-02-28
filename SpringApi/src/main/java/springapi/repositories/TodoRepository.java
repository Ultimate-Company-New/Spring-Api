package springapi.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import springapi.models.databasemodels.Todo;

/** Defines the todo repository contract. */
@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {
  List<Todo> findAllByUserIdOrderByTodoIdDesc(Long userId);
}
