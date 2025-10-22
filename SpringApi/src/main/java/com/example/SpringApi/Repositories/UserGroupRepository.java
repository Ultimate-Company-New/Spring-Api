package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.UserGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserGroupRepository extends JpaRepository<UserGroup, Long> {
    
    /**
     * Finds a user group by its name (case-sensitive).
     * 
     * @param groupName The name of the group to search for
     * @return UserGroups entity if found, null otherwise
     */
    UserGroup findByGroupName(String groupName);
    
    /**
     * Finds all user groups associated with a specific client.
     * 
     * @param clientId The unique identifier of the client
     * @return List of UserGroups entities for the client
     */
    List<UserGroup> findByClientId(Long clientId);
    
    /**
     * Finds all active user groups (where isDeleted = false).
     * 
     * @return List of active UserGroups entities
     */
    List<UserGroup> findByIsDeletedFalse();
    
    /**
     * Gets all user IDs that belong to a specific group.
     * 
     * @param groupId The unique identifier of the user group
     * @return List of user IDs in the group
     */
    @Query("SELECT ugm.userId FROM UserGroupUserMap ugm WHERE ugm.groupId = :groupId")
    List<Long> getUserIdsInGroup(@Param("groupId") long groupId);

    /**
     * Gets all user group IDs that a specific user belongs to.
     * 
     * @param userId The unique identifier of the user
     * @return List of group IDs the user belongs to
     */
    @Query("SELECT ugm.groupId FROM UserGroupUserMap ugm WHERE ugm.userId = :userId")
    List<Long> getUserGroupIdsFromUserId(@Param("userId") long userId);

    /**
     * Finds a user group by ID with all user mappings and users loaded in a single query.
     * This uses JOIN FETCH to eagerly load all related data to avoid N+1 query problem.
     * 
     * @param groupId The unique identifier of the user group
     * @return UserGroup entity with users loaded, or null if not found
     */
    @Query("SELECT DISTINCT ug FROM UserGroup ug " +
           "LEFT JOIN FETCH ug.userMappings ugm " +
           "LEFT JOIN FETCH ugm.user " +
           "WHERE ug.groupId = :groupId")
    UserGroup findByIdWithUsers(@Param("groupId") Long groupId);

    /**
     * Paginated query for user groups with filtering and sorting.
     * Filters by client ID and supports various filter conditions.
     * Used by: UserGroupService.fetchUserGroupsInClientInBatches
     */
    @Query("SELECT ug FROM UserGroup ug " +
        "WHERE ug.clientId = :clientId " +
        "AND (:selectedGroups IS NULL OR ug.groupId IN (:selectedGroups)) " +
        "AND (:includeDeleted = true OR ug.isDeleted = false) " +
        "AND (COALESCE(:filterExpr, '') = '' OR " +
        "(CASE :columnName " +
        "WHEN 'userGroupId' THEN CONCAT(ug.groupId, '') " +
        "WHEN 'name' THEN CONCAT(ug.groupName, '') " +
        "WHEN 'description' THEN CONCAT(ug.description, '') " +
        "ELSE '' END) LIKE " +
        "(CASE :condition " +
        "WHEN 'contains' THEN CONCAT('%', :filterExpr, '%') " +
        "WHEN 'equals' THEN :filterExpr " +
        "WHEN 'startsWith' THEN CONCAT(:filterExpr, '%') " +
        "WHEN 'endsWith' THEN CONCAT('%', :filterExpr) " +
        "WHEN 'isEmpty' THEN '' " +
        "WHEN 'isNotEmpty' THEN '%' " +
        "ELSE '' END))")
    Page<UserGroup> findPaginatedUserGroups(@Param("clientId") long clientId,
                                             @Param("selectedGroups") List<Long> selectedGroups,
                                             @Param("columnName") String columnName,
                                             @Param("condition") String condition,
                                             @Param("filterExpr") String filterExpr,
                                             @Param("includeDeleted") boolean includeDeleted,
                                             Pageable pageable);
}