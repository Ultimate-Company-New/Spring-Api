package springapi.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import springapi.models.databasemodels.UserLog;

/** Defines the user log repository contract. */
@Repository
public interface UserLogRepository extends JpaRepository<UserLog, Long> {}
