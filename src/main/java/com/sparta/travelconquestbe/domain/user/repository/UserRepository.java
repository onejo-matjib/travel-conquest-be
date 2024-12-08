package com.sparta.travelconquestbe.domain.user.repository;

import com.sparta.travelconquestbe.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

}