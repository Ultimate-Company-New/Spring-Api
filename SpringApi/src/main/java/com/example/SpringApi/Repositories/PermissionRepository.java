package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Permission entity operations.
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    
    Optional<Permission> findByPermissionCode(String permissionCode);
    
    Optional<Permission> findByPermissionName(String permissionName);
    
    List<Permission> findByCategory(String category);
    
    List<Permission> findByIsDeletedFalse();
    
    List<Permission> findByCategoryAndIsDeletedFalse(String category, Boolean isDeleted);
}
