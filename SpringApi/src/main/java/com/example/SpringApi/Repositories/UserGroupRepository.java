package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.UserGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
     * Finds user groups with optional filtering and pagination support.
     * Returns user groups along with their member count.
     * 
     * @param columnName The column name to filter on
     * @param condition The filter condition (contains, equals, startsWith, endsWith, isEmpty, isNotEmpty)
     * @param filterExpr The filter expression/value
     * @param includeDeleted Whether to include deleted groups
     * @return List of Object arrays containing [UserGroups, member count]
     */
    @Query("SELECT ug, COUNT(ugm.userId) " +
            "FROM UserGroup ug " +
            "LEFT JOIN UserGroupUserMap ugm ON ug.groupId = ugm.groupId " +
            "WHERE (:includeDeleted = true OR ug.isDeleted = false) " +
            "AND (COALESCE(:filterExpr, '') = '' OR " +
            "(CASE :columnName " +
            "WHEN 'groupId' THEN CONCAT(ug.groupId, '') " +
            "WHEN 'groupName' THEN CONCAT(ug.groupName, '') " +
            "WHEN 'description' THEN CONCAT(ug.description, '') " +
            "ELSE '' END) LIKE " +
            "(CASE :condition " +
            "WHEN 'contains' THEN CONCAT('%', :filterExpr, '%') " +
            "WHEN 'equals' THEN :filterExpr " +
            "WHEN 'startsWith' THEN CONCAT(:filterExpr, '%') " +
            "WHEN 'endsWith' THEN CONCAT('%', :filterExpr) " +
            "WHEN 'isEmpty' THEN '' " +
            "WHEN 'isNotEmpty' THEN '%' " +
            "ELSE '' END)) " +
            "GROUP BY ug")
    List<Object[]> findUserGroups(@Param("columnName") String columnName,
                                   @Param("condition") String condition,
                                   @Param("filterExpr") String filterExpr,
                                   @Param("includeDeleted") boolean includeDeleted);

    /**
     * Finds user groups with pagination, filtering, and custom sorting.
     * Allows prioritizing specific group IDs in the results.
     * 
     * @param columnName The column name to filter on
     * @param condition The filter condition
     * @param filterExpr The filter expression/value
     * @param includeDeleted Whether to include deleted groups
     * @param groupIds List of group IDs to prioritize in sorting
     * @param pageable Pagination information
     * @return Page of Object arrays containing [UserGroups, member count]
     */
    default Page<Object[]> findPaginatedUserGroups(String columnName,
                                                    String condition,
                                                    String filterExpr,
                                                    boolean includeDeleted,
                                                    List<Long> groupIds,
                                                    Pageable pageable) {
        // Fetch sorted data
        List<Object[]> data = findUserGroups(columnName, condition, filterExpr, includeDeleted);
        data.sort((obj1, obj2) -> {
            UserGroup group1 = (UserGroup) obj1[0];
            UserGroup group2 = (UserGroup) obj2[0];

            // Check if group1's groupId is present in groupIds
            boolean group1InGroupIds = groupIds != null && !groupIds.isEmpty() && groupIds.contains(group1.getGroupId());
            // Check if group2's groupId is present in groupIds
            boolean group2InGroupIds = groupIds != null && !groupIds.isEmpty() && groupIds.contains(group2.getGroupId());

            // If group1 is in groupIds but group2 is not, group1 should come before group2
            if (group1InGroupIds && !group2InGroupIds) {
                return -1;
            }
            // If group2 is in groupIds but group1 is not, group2 should come before group1
            else if (!group1InGroupIds && group2InGroupIds) {
                return 1;
            }
            // If both groups are in groupIds or both are not, compare their groupIds
            else {
                return Long.compare(group2.getGroupId(), group1.getGroupId());
            }
        });

        // Apply pagination to the sorted data
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), data.size());
        return new PageImpl<>(data.subList(start, end), pageable, data.size());
    }
}