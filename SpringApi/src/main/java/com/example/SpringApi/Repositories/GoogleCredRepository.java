package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.GoogleCred;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GoogleCredRepository extends JpaRepository<GoogleCred, Long> {}

