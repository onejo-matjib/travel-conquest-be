package com.sparta.travelconquestbe.domain.routelocation.repository;

import com.sparta.travelconquestbe.domain.routelocation.entity.RouteLocation;

import java.util.List;

public interface RouteLocationJdbcRepository {
   void bulkSave(List<RouteLocation> locations);
}
