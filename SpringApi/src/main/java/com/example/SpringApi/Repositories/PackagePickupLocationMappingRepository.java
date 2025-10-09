package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.PackagePickupLocationMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackagePickupLocationMappingRepository extends JpaRepository<PackagePickupLocationMapping, Long> {
    List<PackagePickupLocationMapping> findByPickupLocationId(Long pickupLocationId);
}