package com.example.springapi.repositories;

import com.example.springapi.models.databasemodels.GoogleCred;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Defines the google cred repository contract.
 */
@Repository
public interface GoogleCredRepository extends JpaRepository<GoogleCred, Long> {}
