package com.example.springapi.repositories;

import com.example.springapi.models.databasemodels.UserLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Defines the user log repository contract.
 */
@Repository
public interface UserLogRepository extends JpaRepository<UserLog, Long> {}
