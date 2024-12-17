package com.sparta.travelconquestbe.domain.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.travelconquestbe.domain.chat.entity.Chat;

public interface ChatRepository extends JpaRepository<Chat, Long> {
}
