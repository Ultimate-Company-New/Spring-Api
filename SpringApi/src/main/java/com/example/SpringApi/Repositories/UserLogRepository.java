package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.UserLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserLogRepository extends JpaRepository<UserLog, Long> {}

