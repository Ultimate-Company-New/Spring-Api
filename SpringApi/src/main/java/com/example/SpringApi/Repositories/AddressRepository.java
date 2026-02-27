package com.example.springapi.repositories;

import com.example.springapi.models.databasemodels.Address;
import com.example.springapi.models.dtos.AddressDuplicateCriteria;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Defines the address repository contract.
 */
@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
  @Query(
      "SELECT a FROM Address a WHERE a.userId = :userId AND a.isDeleted = :"
          + "isDeleted ORDER BY a.addressId DESC")
  List<Address> findByUserIdAndIsDeletedOrderByAddressIdDesc(
      @Param("userId") long userId, @Param("isDeleted") boolean isDeleted);

  @Query(
      "SELECT a FROM Address a WHERE a.clientId = :clientId AND "
          + "a.isDeleted = :isDeleted ORDER BY a.addressId DESC")
  List<Address> findByClientIdAndIsDeletedOrderByAddressIdDesc(
      @Param("clientId") long clientId, @Param("isDeleted") boolean isDeleted);

  /**
   * Executes or.
   */
  @Query(
      "SELECT a FROM Address a WHERE "
          + "(a.userId = :#{#criteria.userId} OR (a.userId IS NULL AND :"
          + "#{#criteria.userId} IS NULL)) AND "
          + "(a.clientId = :#{#criteria.clientId} OR (a.clientId IS NULL AND :"
          + "#{#criteria.clientId} IS NULL)) AND "
          + "a.addressType = :#{#criteria.addressType} AND "
          + "a.streetAddress = :#{#criteria.streetAddress} AND "
          + "(a.streetAddress2 = :#{#criteria.streetAddress2} OR ("
          + "a.streetAddress2 IS NULL AND :#{#criteria.streetAddress2} IS NULL)) "
          + "AND "
          + "(a.streetAddress3 = :#{#criteria.streetAddress3} OR ("
          + "a.streetAddress3 IS NULL AND :#{#criteria.streetAddress3} IS NULL)) "
          + "AND "
          + "a.city = :#{#criteria.city} AND "
          + "a.state = :#{#criteria.state} AND "
          + "a.postalCode = :#{#criteria.postalCode} AND "
          + "(a.nameOnAddress = :#{#criteria.nameOnAddress} OR (a.nameOnAddress "
          + "IS NULL AND :#{#criteria.nameOnAddress} IS NULL)) AND "
          + "(a.emailOnAddress = :#{#criteria.emailOnAddress} OR ("
          + "a.emailOnAddress IS NULL AND :#{#criteria.emailOnAddress} IS NULL)) "
          + "AND "
          + "(a.phoneOnAddress = :#{#criteria.phoneOnAddress} OR ("
          + "a.phoneOnAddress IS NULL AND :#{#criteria.phoneOnAddress} IS NULL)) "
          + "AND "
          + "a.country = :#{#criteria.country} AND "
          + "a.isPrimary = :#{#criteria.isPrimary} AND "
          + "a.isDeleted = :#{#criteria.isDeleted}")
  java.util.Optional<Address> findExactDuplicate(
      @Param("criteria") AddressDuplicateCriteria criteria);
}
