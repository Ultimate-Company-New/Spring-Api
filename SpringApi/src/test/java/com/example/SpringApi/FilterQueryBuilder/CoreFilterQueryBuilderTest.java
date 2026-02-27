package com.example.SpringApi.FilterQueryBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.SpringApi.Models.DatabaseModels.Lead;
import com.example.SpringApi.Models.DatabaseModels.PickupLocation;
import com.example.SpringApi.Models.DatabaseModels.ProductReview;
import com.example.SpringApi.Models.DatabaseModels.Promo;
import com.example.SpringApi.Models.DatabaseModels.Shipment;
import com.example.SpringApi.Models.DatabaseModels.User;
import com.example.SpringApi.Models.DatabaseModels.UserGroup;
import com.example.SpringApi.Models.DatabaseModels.UserLog;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel.FilterCondition;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;

@DisplayName("Core Filter Query Builder Tests")
class CoreFilterQueryBuilderTest extends FilterQueryBuilderTestBase {

  // Total Tests: 8

  /**
   * Purpose: Verify lead query builder builds correct type mapping and paginated query clauses.
   * Expected Result: Correct page output and query string with includeDeleted and filter
   * constraints. Assertions: Column types, total elements, and query fragments.
   */
  @Test
  @DisplayName("leadFilterQueryBuilder - Query Generation - Success")
  void leadFilterQueryBuilder_s01_queryGeneration_success() {
    // Arrange
    LeadFilterQueryBuilder builder = new LeadFilterQueryBuilder(entityManager);
    Lead lead = new Lead();
    lead.setLeadId(11L);
    QueryFixture<Lead> fixture = stubPagedQueries(Lead.class, List.of(lead), 21L);
    List<FilterCondition> filters = List.of(createFilter("firstName", "contains", "nahush"));

    // Act
    Page<Lead> page =
        builder.findPaginatedEntitiesWithMultipleFilters(
            1L, "AND", filters, false, createPageable(0, 10));

    // Assert
    assertEquals("date", builder.getColumnType("createdAt"));
    assertEquals("boolean", builder.getColumnType("isDeleted"));
    assertEquals("number", builder.getColumnType("leadId"));
    assertEquals("string", builder.getColumnType("unknown"));
    assertEquals(1, page.getContent().size());
    assertEquals(21L, page.getTotalElements());
    assertTrue(fixture.getMainQueryString().contains("AND l.isDeleted = false"));
    assertTrue(fixture.getMainQueryString().contains("LOWER(l.firstName) LIKE LOWER(:param0)"));
    assertTrue(fixture.getCountQueryString().contains("COUNT(DISTINCT l)"));
  }

  /**
   * Purpose: Verify promo query builder applies selectedIds and includeDeleted rules correctly.
   * Expected Result: Generated query includes selectedIds, excludes deleted, and applies filters.
   * Assertions: Column type mapping, page count, and main/count query fragments.
   */
  @Test
  @DisplayName("promoFilterQueryBuilder - SelectedIds And Filters - Success")
  void promoFilterQueryBuilder_s02_selectedIdsAndFilters_success() {
    // Arrange
    PromoFilterQueryBuilder builder = new PromoFilterQueryBuilder(entityManager);
    Promo promo = new Promo();
    promo.setPromoId(19L);
    QueryFixture<Promo> fixture = stubPagedQueries(Promo.class, List.of(promo), 21L);
    List<FilterCondition> filters = List.of(createFilter("promoCode", "contains", "DIWALI"));

    // Act
    Page<Promo> page =
        builder.findPaginatedEntitiesWithMultipleFilters(
            1L, List.of(19L, 25L), "OR", filters, false, createPageable(1, 5));

    // Assert
    assertEquals("number", builder.getColumnType("promoId"));
    assertEquals("boolean", builder.getColumnType("isDeleted"));
    assertEquals(1, page.getContent().size());
    assertEquals(21L, page.getTotalElements());
    assertTrue(fixture.getMainQueryString().contains("p.promoId IN :selectedIds"));
    assertTrue(fixture.getMainQueryString().contains("AND p.isDeleted = false"));
    assertTrue(fixture.getCountQueryString().contains("LOWER(p.promoCode) LIKE LOWER(:param0)"));
  }

  /**
   * Purpose: Verify product review query builder handles client, product, selectedIds, and dynamic
   * filters. Expected Result: Query contains product filter branch and selected ID branch.
   * Assertions: Column type mapping, page totals, and query fragments.
   */
  @Test
  @DisplayName("productReviewFilterQueryBuilder - Product Scope And Filters - Success")
  void productReviewFilterQueryBuilder_s03_productScopeAndFilters_success() {
    // Arrange
    ProductReviewFilterQueryBuilder builder = new ProductReviewFilterQueryBuilder(entityManager);
    ProductReview review = new ProductReview();
    review.setReviewId(55L);
    QueryFixture<ProductReview> fixture =
        stubPagedQueries(ProductReview.class, List.of(review), 21L);
    List<FilterCondition> filters = List.of(createFilter("score", "greaterThanOrEqual", "4"));

    // Act
    Page<ProductReview> page =
        builder.findPaginatedEntitiesWithMultipleFilters(
            1L, 99L, List.of(55L), "AND", filters, false, createPageable(0, 20));

    // Assert
    assertEquals("number", builder.getColumnType("score"));
    assertEquals("date", builder.getColumnType("createdAt"));
    assertEquals(1, page.getContent().size());
    assertEquals(21L, page.getTotalElements());
    assertTrue(fixture.getMainQueryString().contains("pr.productId = :productId"));
    assertTrue(fixture.getMainQueryString().contains("pr.reviewId IN :selectedIds"));
    assertTrue(fixture.getMainQueryString().contains("pr.isDeleted = false"));
    assertTrue(fixture.getCountQueryString().contains("pr.score >= :param0"));
  }

  /**
   * Purpose: Verify shipment query builder enforces shipRocket order requirement and selected IDs.
   * Expected Result: Queries include not-null ShipRocket order predicates and selectedIds.
   * Assertions: Column type mapping and query fragments.
   */
  @Test
  @DisplayName("shipmentFilterQueryBuilder - ShipRocket Eligibility Filter - Success")
  void shipmentFilterQueryBuilder_s04_shipRocketEligibilityFilter_success() {
    // Arrange
    ShipmentFilterQueryBuilder builder = new ShipmentFilterQueryBuilder(entityManager);
    Shipment shipment = new Shipment();
    shipment.setShipmentId(10L);
    QueryFixture<Shipment> fixture = stubPagedQueries(Shipment.class, List.of(shipment), 21L);
    List<FilterCondition> filters =
        List.of(createFilter("shipRocketStatus", "contains", "TRANSIT"));

    // Act
    Page<Shipment> page =
        builder.findPaginatedEntitiesWithMultipleFilters(
            1L, List.of(10L, 11L), "AND", filters, createPageable(0, 10));

    // Assert
    assertEquals("number", builder.getColumnType("totalCost"));
    assertEquals("date", builder.getColumnType("createdAt"));
    assertEquals(1, page.getContent().size());
    assertEquals(21L, page.getTotalElements());
    assertTrue(fixture.getMainQueryString().contains("s.shipRocketOrderId IS NOT NULL"));
    assertTrue(fixture.getMainQueryString().contains("s.shipmentId IN :selectedIds"));
    assertTrue(
        fixture.getCountQueryString().contains("LOWER(s.shipRocketStatus) LIKE LOWER(:param0)"));
  }

  /**
   * Purpose: Verify user query builder joins and includeDeleted/selectedIds constraints. Expected
   * Result: Query includes mapping joins, selected IDs, and not-deleted condition. Assertions: Type
   * mapping and key query predicates.
   */
  @Test
  @DisplayName("userFilterQueryBuilder - Joins And Selection Constraints - Success")
  void userFilterQueryBuilder_s05_joinsAndSelectionConstraints_success() {
    // Arrange
    UserFilterQueryBuilder builder = new UserFilterQueryBuilder(entityManager);
    User user = new User();
    user.setUserId(5L);
    QueryFixture<User> fixture = stubPagedQueries(User.class, List.of(user), 30L);
    List<FilterCondition> filters = List.of(createFilter("firstName", "contains", "rahul"));

    // Act
    Page<User> page =
        builder.findPaginatedEntitiesWithMultipleFilters(
            1L, List.of(5L), "AND", filters, false, createPageable(0, 25));

    // Assert
    assertEquals("boolean", builder.getColumnType("locked"));
    assertEquals("number", builder.getColumnType("userId"));
    assertEquals(1, page.getContent().size());
    assertEquals(30L, page.getTotalElements());
    assertTrue(fixture.getMainQueryString().contains("JOIN UserClientMapping ucm"));
    assertTrue(fixture.getMainQueryString().contains("AND u.userId IN :selectedIds"));
    assertTrue(fixture.getMainQueryString().contains("AND u.isDeleted = false"));
    assertTrue(fixture.getCountQueryString().contains("LOWER(u.firstName) LIKE LOWER(:param0)"));
  }

  /**
   * Purpose: Verify user group query builder handles aggregate mapping and boolean filters.
   * Expected Result: Query includes selectedIds, includeDeleted, and dynamic filter condition.
   * Assertions: Type mapping and query fragments.
   */
  @Test
  @DisplayName("userGroupFilterQueryBuilder - Group Filters And Paging - Success")
  void userGroupFilterQueryBuilder_s06_groupFiltersAndPaging_success() {
    // Arrange
    UserGroupFilterQueryBuilder builder = new UserGroupFilterQueryBuilder(entityManager);
    UserGroup group = new UserGroup();
    group.setGroupId(32L);
    QueryFixture<UserGroup> fixture = stubPagedQueries(UserGroup.class, List.of(group), 21L);
    List<FilterCondition> filters = List.of(createFilter("groupName", "contains", "ops"));

    // Act
    Page<UserGroup> page =
        builder.findPaginatedEntitiesWithMultipleFilters(
            1L, List.of(32L, 33L), "AND", filters, false, createPageable(0, 15));

    // Assert
    assertEquals("number", builder.getColumnType("members"));
    assertEquals("boolean", builder.getColumnType("isActive"));
    assertEquals(1, page.getContent().size());
    assertEquals(21L, page.getTotalElements());
    assertTrue(fixture.getMainQueryString().contains("ug.groupId IN :selectedIds"));
    assertTrue(fixture.getMainQueryString().contains("ug.isDeleted = false"));
    assertTrue(fixture.getCountQueryString().contains("LOWER(ug.groupName) LIKE LOWER(:param0)"));
  }

  /**
   * Purpose: Verify user log query builder enforces user/client scoping and filter application.
   * Expected Result: Query includes user/client predicates and dynamic condition. Assertions: Type
   * mapping and scoping query fragments.
   */
  @Test
  @DisplayName("userLogFilterQueryBuilder - User Scope And Filters - Success")
  void userLogFilterQueryBuilder_s07_userScopeAndFilters_success() {
    // Arrange
    UserLogFilterQueryBuilder builder = new UserLogFilterQueryBuilder(entityManager);
    UserLog log = new UserLog();
    log.setLogId(900L);
    QueryFixture<UserLog> fixture = stubPagedQueries(UserLog.class, List.of(log), 60L);
    List<FilterCondition> filters = List.of(createFilter("action", "contains", "LOGIN"));

    // Act
    Page<UserLog> page =
        builder.findPaginatedEntitiesWithMultipleFilters(
            77L, 1L, "AND", filters, createPageable(0, 50));

    // Assert
    assertEquals("number", builder.getColumnType("auditUserId"));
    assertEquals("string", builder.getColumnType("action"));
    assertEquals(1, page.getContent().size());
    assertEquals(60L, page.getTotalElements());
    assertTrue(fixture.getMainQueryString().contains("ul.userId = :userId"));
    assertTrue(
        fixture.getMainQueryString().contains("ul.clientId = :clientId OR ul.clientId IS NULL"));
    assertTrue(fixture.getCountQueryString().contains("LOWER(ul.action) LIKE LOWER(:param0)"));
  }

  /**
   * Purpose: Verify pickup location query builder applies selectedIds, includeDeleted, and address
   * joins. Expected Result: Query uses address join and dynamic filters with pagination.
   * Assertions: Type mapping and expected query fragments.
   */
  @Test
  @DisplayName("pickupLocationFilterQueryBuilder - Address Join And Filters - Success")
  void pickupLocationFilterQueryBuilder_s08_addressJoinAndFilters_success() {
    // Arrange
    PickupLocationFilterQueryBuilder builder = new PickupLocationFilterQueryBuilder(entityManager);
    PickupLocation pickupLocation = new PickupLocation();
    pickupLocation.setPickupLocationId(501L);
    QueryFixture<PickupLocation> fixture =
        stubPagedQueries(PickupLocation.class, List.of(pickupLocation), 1L);
    List<FilterCondition> filters =
        List.of(createFilter("addressNickName", "contains", "Warehouse"));

    // Act
    Page<PickupLocation> page =
        builder.findPaginatedEntitiesWithMultipleFilters(
            1L, List.of(501L), "AND", filters, false, createPageable(0, 20));

    // Assert
    assertEquals("number", builder.getColumnType("pickupLocationId"));
    assertEquals("boolean", builder.getColumnType("isDeleted"));
    assertEquals(1, page.getContent().size());
    assertEquals(1L, page.getTotalElements());
    assertTrue(fixture.getMainQueryString().contains("JOIN FETCH pl.address a"));
    assertTrue(fixture.getMainQueryString().contains("pl.pickupLocationId IN :selectedIds"));
    assertTrue(fixture.getMainQueryString().contains("pl.isDeleted = false"));
    assertTrue(
        fixture.getCountQueryString().contains("LOWER(pl.addressNickName) LIKE LOWER(:param0)"));
  }
}

