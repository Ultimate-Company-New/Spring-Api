package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.ReturnShipmentProduct;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReturnShipmentProductRepository
    extends JpaRepository<ReturnShipmentProduct, Long> {
  List<ReturnShipmentProduct> findByReturnShipmentId(Long returnShipmentId);

  List<ReturnShipmentProduct> findByReturnShipmentIdAndClientId(
      Long returnShipmentId, Long clientId);

  List<ReturnShipmentProduct> findByReturnShipmentIdAndIsDeletedFalse(Long returnShipmentId);
}
