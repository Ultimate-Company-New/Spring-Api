package com.example.SpringApi.FilterQueryBuilder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.SpringApi.Models.DatabaseModels.Payment;
import com.example.SpringApi.Models.DatabaseModels.PurchaseOrder;
import com.example.SpringApi.Models.DatabaseModels.Resources;
import com.example.SpringApi.Models.DatabaseModels.ShipmentPackage;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel.FilterCondition;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
abstract class FilterQueryBuilderTestBase {

  @Mock protected EntityManager entityManager;

  protected FilterCondition createFilter(String column, String operator, Object value) {
    FilterCondition filterCondition = new FilterCondition();
    filterCondition.setColumn(column);
    filterCondition.setOperator(operator);
    filterCondition.setValue(value);
    return filterCondition;
  }

  protected Pageable createPageable(int page, int size) {
    return PageRequest.of(page, size);
  }

  protected <T> QueryFixture<T> stubPagedQueries(
      Class<T> entityClass, List<T> resultList, long totalCount) {
    TypedQuery<Long> countQuery = mock(TypedQuery.class);
    TypedQuery<T> mainQuery = mock(TypedQuery.class);

    AtomicReference<String> capturedCountQuery = new AtomicReference<>();
    AtomicReference<String> capturedMainQuery = new AtomicReference<>();

    when(entityManager.createQuery(anyString(), eq(Long.class)))
        .thenAnswer(
            invocation -> {
              capturedCountQuery.set(invocation.getArgument(0, String.class));
              return countQuery;
            });

    when(entityManager.createQuery(anyString(), eq(entityClass)))
        .thenAnswer(
            invocation -> {
              capturedMainQuery.set(invocation.getArgument(0, String.class));
              return mainQuery;
            });

    when(countQuery.setParameter(anyString(), any())).thenReturn(countQuery);
    when(mainQuery.setParameter(anyString(), any())).thenReturn(mainQuery);
    when(mainQuery.setFirstResult(anyInt())).thenReturn(mainQuery);
    when(mainQuery.setMaxResults(anyInt())).thenReturn(mainQuery);

    when(countQuery.getSingleResult()).thenReturn(totalCount);
    when(mainQuery.getResultList()).thenReturn(resultList);

    return new QueryFixture<>(countQuery, mainQuery, capturedCountQuery, capturedMainQuery);
  }

  protected PurchaseOrderDetailsQueryFixture stubPurchaseOrderDetailsQueries(
      List<PurchaseOrder> purchaseOrders,
      long totalCount,
      List<ShipmentPackage> shipmentPackages,
      List<Resources> resources,
      List<Payment> payments) {
    TypedQuery<Long> countQuery = mock(TypedQuery.class);
    TypedQuery<PurchaseOrder> mainQuery = mock(TypedQuery.class);
    TypedQuery<ShipmentPackage> shipmentPackageQuery = mock(TypedQuery.class);
    TypedQuery<Resources> resourcesQuery = mock(TypedQuery.class);
    TypedQuery<Payment> paymentsQuery = mock(TypedQuery.class);

    AtomicReference<String> capturedCountQuery = new AtomicReference<>();
    AtomicReference<String> capturedMainQuery = new AtomicReference<>();
    AtomicReference<String> capturedShipmentPackageQuery = new AtomicReference<>();
    AtomicReference<String> capturedResourcesQuery = new AtomicReference<>();
    AtomicReference<String> capturedPaymentsQuery = new AtomicReference<>();

    when(entityManager.createQuery(anyString(), eq(Long.class)))
        .thenAnswer(
            invocation -> {
              capturedCountQuery.set(invocation.getArgument(0, String.class));
              return countQuery;
            });

    when(entityManager.createQuery(anyString(), eq(PurchaseOrder.class)))
        .thenAnswer(
            invocation -> {
              capturedMainQuery.set(invocation.getArgument(0, String.class));
              return mainQuery;
            });

    when(entityManager.createQuery(anyString(), eq(ShipmentPackage.class)))
        .thenAnswer(
            invocation -> {
              capturedShipmentPackageQuery.set(invocation.getArgument(0, String.class));
              return shipmentPackageQuery;
            });

    when(entityManager.createQuery(anyString(), eq(Resources.class)))
        .thenAnswer(
            invocation -> {
              capturedResourcesQuery.set(invocation.getArgument(0, String.class));
              return resourcesQuery;
            });

    when(entityManager.createQuery(anyString(), eq(Payment.class)))
        .thenAnswer(
            invocation -> {
              capturedPaymentsQuery.set(invocation.getArgument(0, String.class));
              return paymentsQuery;
            });

    when(countQuery.setParameter(anyString(), any())).thenReturn(countQuery);
    when(mainQuery.setParameter(anyString(), any())).thenReturn(mainQuery);
    when(mainQuery.setFirstResult(anyInt())).thenReturn(mainQuery);
    when(mainQuery.setMaxResults(anyInt())).thenReturn(mainQuery);
    when(shipmentPackageQuery.setParameter(anyString(), any())).thenReturn(shipmentPackageQuery);
    when(resourcesQuery.setParameter(anyString(), any())).thenReturn(resourcesQuery);
    when(paymentsQuery.setParameter(anyString(), any())).thenReturn(paymentsQuery);

    when(countQuery.getSingleResult()).thenReturn(totalCount);
    when(mainQuery.getResultList()).thenReturn(purchaseOrders);
    when(shipmentPackageQuery.getResultList()).thenReturn(shipmentPackages);
    when(resourcesQuery.getResultList()).thenReturn(resources);
    when(paymentsQuery.getResultList()).thenReturn(payments);

    return new PurchaseOrderDetailsQueryFixture(
        capturedCountQuery,
        capturedMainQuery,
        capturedShipmentPackageQuery,
        capturedResourcesQuery,
        capturedPaymentsQuery);
  }

  protected static final class QueryFixture<T> {
    private final TypedQuery<Long> countQuery;
    private final TypedQuery<T> mainQuery;
    private final AtomicReference<String> countQueryString;
    private final AtomicReference<String> mainQueryString;

    QueryFixture(
        TypedQuery<Long> countQuery,
        TypedQuery<T> mainQuery,
        AtomicReference<String> countQueryString,
        AtomicReference<String> mainQueryString) {
      this.countQuery = countQuery;
      this.mainQuery = mainQuery;
      this.countQueryString = countQueryString;
      this.mainQueryString = mainQueryString;
    }

    public TypedQuery<Long> getCountQuery() {
      return countQuery;
    }

    public TypedQuery<T> getMainQuery() {
      return mainQuery;
    }

    public String getCountQueryString() {
      return countQueryString.get();
    }

    public String getMainQueryString() {
      return mainQueryString.get();
    }
  }

  protected static final class PurchaseOrderDetailsQueryFixture {
    private final AtomicReference<String> countQueryString;
    private final AtomicReference<String> mainQueryString;
    private final AtomicReference<String> shipmentPackageQueryString;
    private final AtomicReference<String> resourcesQueryString;
    private final AtomicReference<String> paymentsQueryString;

    PurchaseOrderDetailsQueryFixture(
        AtomicReference<String> countQueryString,
        AtomicReference<String> mainQueryString,
        AtomicReference<String> shipmentPackageQueryString,
        AtomicReference<String> resourcesQueryString,
        AtomicReference<String> paymentsQueryString) {
      this.countQueryString = countQueryString;
      this.mainQueryString = mainQueryString;
      this.shipmentPackageQueryString = shipmentPackageQueryString;
      this.resourcesQueryString = resourcesQueryString;
      this.paymentsQueryString = paymentsQueryString;
    }

    public String getCountQueryString() {
      return countQueryString.get();
    }

    public String getMainQueryString() {
      return mainQueryString.get();
    }

    public String getShipmentPackageQueryString() {
      return shipmentPackageQueryString.get();
    }

    public String getResourcesQueryString() {
      return resourcesQueryString.get();
    }

    public String getPaymentsQueryString() {
      return paymentsQueryString.get();
    }
  }
}
