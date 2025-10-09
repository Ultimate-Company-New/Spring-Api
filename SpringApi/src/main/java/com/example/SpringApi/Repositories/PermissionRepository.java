package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.Permissions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermissionRepository extends JpaRepository<Permissions, Long> {
    
    List<Permissions> findByCategory(String category);
    
    List<Permissions> findByIsActiveTrue();
}
