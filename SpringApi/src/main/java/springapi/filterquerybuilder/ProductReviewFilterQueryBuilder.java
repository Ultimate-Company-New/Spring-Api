package springapi.filterquerybuilder;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import springapi.models.databasemodels.ProductReview;
import springapi.models.requestmodels.PaginationBaseRequestModel.FilterCondition;

/**
 * ProductReview-specific filter query builder that handles dynamic multi-filter queries.
 *
 * <p>This class combines: 1. Column mapping logic (which columns map to which database fields) 2.
 * Query building logic (using BaseFilterQueryBuilder) 3. Query execution logic (executing the built
 * queries via EntityManager)
 */
@Component
public class ProductReviewFilterQueryBuilder extends BaseFilterQueryBuilder {
  private static final String PRODUCT_ID = "productId";
  private static final String PRODUCT_ID_PARAM = ":" + PRODUCT_ID;

  private final EntityManager entityManager;

  public ProductReviewFilterQueryBuilder(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  protected String mapColumnToField(String column) {
    switch (column) {
      case "reviewId":
        return "pr.reviewId";
      case "ratings":
        return "pr.ratings";
      case "score":
        return "pr.score";
      case "isDeleted":
        return "pr.isDeleted";
      case "review":
        return "pr.review";
      case "userId":
        return "pr.userId";
      case PRODUCT_ID:
        return "pr.productId";
      case "parentId":
        return "pr.parentId";
      case "createdUser":
        return "pr.createdUser";
      case "modifiedUser":
        return "pr.modifiedUser";
      case "createdAt":
        return "pr.createdAt";
      case "updatedAt":
        return "pr.updatedAt";
      case "notes":
        return "pr.notes";
      default:
        throw new IllegalArgumentException("Invalid filter column: " + column);
    }
  }

  @Override
  protected List<String> getDateColumns() {
    return Arrays.asList("createdAt", "updatedAt");
  }

  @Override
  protected List<String> getBooleanColumns() {
    return Arrays.asList("isDeleted");
  }

  @Override
  protected List<String> getNumberColumns() {
    return Arrays.asList("reviewId", "ratings", "score", "userId", PRODUCT_ID, "parentId");
  }

  /**
   * Gets the column type for validation purposes.
   *
   * @param column The column name
   * @return "string", "number", "date", or "boolean"
   */
  public String getColumnType(String column) {
    if (getDateColumns().contains(column)) {
      return "date";
    } else if (getBooleanColumns().contains(column)) {
      return "boolean";
    } else if (getNumberColumns().contains(column)) {
      return "number";
    } else {
      return "string";
    }
  }

  /**
   * Finds paginated product reviews with multiple filter conditions. Filters by clientId (via.
   * Product) and optionally by productId.
   *
   * @param clientId The client ID to filter by (via Product)
   * @param productId The product ID to filter reviews for (null for all products of client)
   * @param selectedIds List of specific review IDs to include (null for all)
   * @param logicOperator "AND" or "OR" to combine filter conditions
   * @param filters List of filter conditions to apply
   * @param includeDeleted Whether to include deleted reviews
   * @param pageable Pagination parameters
   * @return Page of product reviews matching the filter criteria
   */
  @SuppressWarnings("java:S2077")
  public Page<ProductReview> findPaginatedEntitiesWithMultipleFilters(
      Long clientId,
      Long productId,
      List<Long> selectedIds,
      String logicOperator,
      List<FilterCondition> filters,
      boolean includeDeleted,
      Pageable pageable) {

    String baseQuery =
        "SELECT pr FROM ProductReview pr "
            + "WHERE pr.productId IN (SELECT p.productId FROM Product p WHERE "
            + "p.clientId = :clientId) ";

    if (productId != null) {
      baseQuery += "AND pr.productId = " + PRODUCT_ID_PARAM + " ";
    }

    if (selectedIds != null && !selectedIds.isEmpty()) {
      baseQuery += "AND pr.reviewId IN :selectedIds ";
    }

    if (!includeDeleted) {
      baseQuery += "AND pr.isDeleted = false ";
    }

    QueryResult filterResult = buildFilterConditions(filters, logicOperator);

    if (filterResult.hasConditions()) {
      baseQuery += "AND (" + filterResult.getWhereClause() + ") ";
    }

    baseQuery += "ORDER BY pr.reviewId DESC";

    String countQuery =
        "SELECT COUNT(pr) FROM ProductReview pr "
            + "WHERE pr.productId IN (SELECT p.productId FROM Product p WHERE "
            + "p.clientId = :clientId) ";

    if (productId != null) {
      countQuery += "AND pr.productId = " + PRODUCT_ID_PARAM + " ";
    }

    if (selectedIds != null && !selectedIds.isEmpty()) {
      countQuery += "AND pr.reviewId IN :selectedIds ";
    }

    if (!includeDeleted) {
      countQuery += "AND pr.isDeleted = false ";
    }

    if (filterResult.hasConditions()) {
      countQuery += "AND (" + filterResult.getWhereClause() + ") ";
    }

    TypedQuery<Long> countTypedQuery = entityManager.createQuery(countQuery, Long.class); // NOSONAR
    countTypedQuery.setParameter("clientId", clientId);
    if (productId != null) {
      countTypedQuery.setParameter(PRODUCT_ID, productId);
    }
    if (selectedIds != null && !selectedIds.isEmpty()) {
      countTypedQuery.setParameter("selectedIds", selectedIds);
    }
    for (Map.Entry<String, Object> entry : filterResult.getParameters().entrySet()) {
      countTypedQuery.setParameter(entry.getKey(), entry.getValue());
    }

    final Long totalCount = countTypedQuery.getSingleResult();

    TypedQuery<ProductReview> mainQuery =
        entityManager.createQuery(baseQuery, ProductReview.class); // NOSONAR
    mainQuery.setParameter("clientId", clientId);
    if (productId != null) {
      mainQuery.setParameter(PRODUCT_ID, productId);
    }
    if (selectedIds != null && !selectedIds.isEmpty()) {
      mainQuery.setParameter("selectedIds", selectedIds);
    }
    for (Map.Entry<String, Object> entry : filterResult.getParameters().entrySet()) {
      mainQuery.setParameter(entry.getKey(), entry.getValue());
    }

    mainQuery.setFirstResult((int) pageable.getOffset());
    mainQuery.setMaxResults(pageable.getPageSize());

    List<ProductReview> reviews = mainQuery.getResultList();

    return new PageImpl<>(reviews, pageable, totalCount);
  }
}
