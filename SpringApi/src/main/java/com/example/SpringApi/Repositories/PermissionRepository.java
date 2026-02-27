package com.example.springapi.repositories;

import com.example.springapi.models.databasemodels.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repository interface for Permission entity operations. */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {}
