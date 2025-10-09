package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.UserClientMapping;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserClientMappingRepository extends JpaRepository<UserClientMapping, Long> {

    @Query("SELECT u FROM UserClientMapping u WHERE u.userId IN :userIds AND u.clientId = :clientId")
    List<UserClientMapping> findByUserIdsAndClientId(@Param("userIds") List<Long> userIds,
                                                       @Param("clientId") Long clientId);
}


