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
    
    /**
     * Finds an exact duplicate address matching all fields except notes, createdUser, modifiedUser, and audit timestamps.
     * Used to avoid creating duplicate addresses.
     * 
     * @param userId The user ID (can be null)
     * @param clientId The client ID (can be null)
     * @param addressType The address type
     * @param streetAddress The street address
     * @param streetAddress2 The street address 2 (can be null)
     * @param streetAddress3 The street address 3 (can be null)
     * @param city The city
     * @param state The state
     * @param postalCode The postal code
     * @param nameOnAddress The name on address (can be null)
     * @param emailOnAddress The email on address (can be null)
     * @param phoneOnAddress The phone on address (can be null)
     * @param country The country
     * @param isPrimary Whether it's primary
     * @param isDeleted Whether it's deleted
     * @return Optional containing the matching address if found
     */
    @Query("SELECT a FROM Address a WHERE " +
           "(a.userId = :userId OR (a.userId IS NULL AND :userId IS NULL)) AND " +
           "(a.clientId = :clientId OR (a.clientId IS NULL AND :clientId IS NULL)) AND " +
           "a.addressType = :addressType AND " +
           "a.streetAddress = :streetAddress AND " +
           "(a.streetAddress2 = :streetAddress2 OR (a.streetAddress2 IS NULL AND :streetAddress2 IS NULL)) AND " +
           "(a.streetAddress3 = :streetAddress3 OR (a.streetAddress3 IS NULL AND :streetAddress3 IS NULL)) AND " +
           "a.city = :city AND " +
           "a.state = :state AND " +
           "a.postalCode = :postalCode AND " +
           "(a.nameOnAddress = :nameOnAddress OR (a.nameOnAddress IS NULL AND :nameOnAddress IS NULL)) AND " +
           "(a.emailOnAddress = :emailOnAddress OR (a.emailOnAddress IS NULL AND :emailOnAddress IS NULL)) AND " +
           "(a.phoneOnAddress = :phoneOnAddress OR (a.phoneOnAddress IS NULL AND :phoneOnAddress IS NULL)) AND " +
           "a.country = :country AND " +
           "a.isPrimary = :isPrimary AND " +
           "a.isDeleted = :isDeleted")
    java.util.Optional<Address> findExactDuplicate(
        @Param("userId") Long userId,
        @Param("clientId") Long clientId,
        @Param("addressType") String addressType,
        @Param("streetAddress") String streetAddress,
        @Param("streetAddress2") String streetAddress2,
        @Param("streetAddress3") String streetAddress3,
        @Param("city") String city,
        @Param("state") String state,
        @Param("postalCode") String postalCode,
        @Param("nameOnAddress") String nameOnAddress,
        @Param("emailOnAddress") String emailOnAddress,
        @Param("phoneOnAddress") String phoneOnAddress,
        @Param("country") String country,
        @Param("isPrimary") Boolean isPrimary,
        @Param("isDeleted") Boolean isDeleted
    );
}

