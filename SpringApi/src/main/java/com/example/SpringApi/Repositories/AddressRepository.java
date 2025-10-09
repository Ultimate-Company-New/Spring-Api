package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUserId(long userId);
    List<Address> findByClientId(long clientId);
}

