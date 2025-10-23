package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    @Query("SELECT a FROM Address a WHERE a.userId = :userId AND a.isDeleted = :isDeleted ORDER BY a.addressId DESC")
    List<Address> findByUserIdAndIsDeletedOrderByAddressIdDesc(@Param("userId") long userId, @Param("isDeleted") boolean isDeleted);

    @Query("SELECT a FROM Address a WHERE a.clientId = :clientId AND a.isDeleted = :isDeleted ORDER BY a.addressId DESC")
    List<Address> findByClientIdAndIsDeletedOrderByAddressIdDesc(@Param("clientId") long clientId, @Param("isDeleted") boolean isDeleted);
}

