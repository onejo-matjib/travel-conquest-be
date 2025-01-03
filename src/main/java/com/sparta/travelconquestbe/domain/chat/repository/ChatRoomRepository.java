package com.sparta.travelconquestbe.domain.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.travelconquestbe.domain.chat.entity.ChatRoom;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
}
