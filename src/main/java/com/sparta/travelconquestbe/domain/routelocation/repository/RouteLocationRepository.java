package com.sparta.travelconquestbe.domain.routelocation.repository;

import com.sparta.travelconquestbe.domain.routelocation.entity.RouteLocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RouteLocationRepository extends JpaRepository<RouteLocation, Long>,RouteLocationJdbcRepository {}
