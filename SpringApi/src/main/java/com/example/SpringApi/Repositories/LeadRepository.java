package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.Lead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {

    @Query("SELECT l FROM Lead l " +
           "LEFT JOIN FETCH l.address a " +
           "LEFT JOIN FETCH l.createdByUser " +
           "LEFT JOIN FETCH l.assignedAgent " +
           "WHERE l.clientId = :clientId " +
           "AND (:includeDeleted = true OR l.isDeleted = false) " +
           "AND (COALESCE(:filterExpr, '') = '' OR " +
           "(CASE :columnName " +
           "WHEN 'leadId' THEN CONCAT(l.leadId, '') " +
           "WHEN 'firstName' THEN CONCAT(l.firstName, '') " +
           "WHEN 'lastName' THEN CONCAT(l.lastName, '') " +
           "WHEN 'email' THEN CONCAT(l.email, '') " +
           "WHEN 'address' THEN CONCAT(COALESCE(a.streetAddress, ''), ' ', COALESCE(a.streetAddress2, ''), ' ', COALESCE(a.city, ''), ' ', COALESCE(a.state, ''), ' ', COALESCE(a.postalCode, '')) " +
           "WHEN 'website' THEN CONCAT(l.website, '') " +
           "WHEN 'phone' THEN CONCAT(l.phone, '') " +
           "WHEN 'companySize' THEN CONCAT(l.companySize, '') " +
           "WHEN 'title' THEN CONCAT(l.title, '') " +
           "WHEN 'leadStatus' THEN CONCAT(l.leadStatus, '') " +
           "WHEN 'company' THEN CONCAT(l.company, '') " +
           "WHEN 'annualRevenue' THEN CONCAT(l.annualRevenue, '') " +
           "WHEN 'fax' THEN CONCAT(l.fax, '') " +
           "WHEN 'isDeleted' THEN CONCAT(l.isDeleted, '') " +
           "WHEN 'createdUser' THEN CONCAT(l.createdUser, '') " +
           "WHEN 'modifiedUser' THEN CONCAT(l.modifiedUser, '') " +
           "WHEN 'createdAt' THEN CONCAT(l.createdAt, '') " +
           "WHEN 'updatedAt' THEN CONCAT(l.updatedAt, '') " +
           "WHEN 'notes' THEN CONCAT(l.notes, '') " +
           "ELSE '' END) LIKE " +
           "(CASE :condition " +
           "WHEN 'contains' THEN CONCAT('%', :filterExpr, '%') " +
           "WHEN 'equals' THEN :filterExpr " +
           "WHEN 'startsWith' THEN CONCAT(:filterExpr, '%') " +
           "WHEN 'endsWith' THEN CONCAT('%', :filterExpr) " +
           "WHEN 'isEmpty' THEN '' " +
           "WHEN 'isNotEmpty' THEN '%' " +
           "ELSE '' END))")
    Page<Lead> findPaginatedLeads(@Param("clientId") Long clientId,
                                  @Param("columnName") String columnName,
                                  @Param("condition") String condition,
                                  @Param("filterExpr") String filterExpr,
                                  @Param("includeDeleted") boolean includeDeleted,
                                  Pageable pageable);

    @Query("SELECT l FROM Lead l " +
           "LEFT JOIN FETCH l.address " +
           "LEFT JOIN FETCH l.createdByUser " +
           "LEFT JOIN FETCH l.assignedAgent " +
           "WHERE l.leadId = :leadId AND l.clientId = :clientId")
    Lead findLeadWithDetailsById(@Param("leadId") Long leadId, @Param("clientId") Long clientId);

    @Query("SELECT l FROM Lead l " +
           "LEFT JOIN FETCH l.address " +
           "LEFT JOIN FETCH l.createdByUser " +
           "LEFT JOIN FETCH l.assignedAgent " +
           "WHERE l.email = :email AND l.clientId = :clientId")
    Lead findLeadWithDetailsByEmail(@Param("email") String email, @Param("clientId") Long clientId);
}