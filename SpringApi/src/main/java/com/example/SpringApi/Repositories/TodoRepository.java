package com.example.springapi.repositories;

import com.example.springapi.models.databasemodels.Todo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Defines the todo repository contract.
 */
@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {
  List<Todo> findAllByUserIdOrderByTodoIdDesc(Long userId);
}
