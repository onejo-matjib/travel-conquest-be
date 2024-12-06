package com.sparta.travelconquestbe.api.chat.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.travelconquestbe.api.chat.dto.request.ChatCreateRequest;
import com.sparta.travelconquestbe.api.chat.dto.respones.ChatCreateResponse;
import com.sparta.travelconquestbe.api.chat.dto.respones.ChatRoomSearchResponse;
import com.sparta.travelconquestbe.domain.chat.entity.ChatRoom;
import com.sparta.travelconquestbe.domain.chat.repository.ChatRoomRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

	private final ChatRoomRepository chatRoomRepository;

	@Transactional
	public ChatCreateResponse createChatRoom(ChatCreateRequest request) {
		ChatRoom chatRoom = new ChatRoom(request.getTitle(), request.getMaxPlayers());
		chatRoomRepository.save(chatRoom);
		return new ChatCreateResponse(chatRoom);
	}

	public List<ChatRoomSearchResponse> searchChatRooms() {
		return chatRoomRepository.findAll().stream()
			.map(ChatRoomSearchResponse::new)
			.collect(Collectors.toList());
	}
}
