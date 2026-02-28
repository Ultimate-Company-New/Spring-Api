package springapi.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import springapi.models.databasemodels.GoogleCred;

/** Defines the google cred repository contract. */
@Repository
public interface GoogleCredRepository extends JpaRepository<GoogleCred, Long> {}
