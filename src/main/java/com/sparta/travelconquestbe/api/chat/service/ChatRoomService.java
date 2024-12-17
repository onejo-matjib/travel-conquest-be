package com.sparta.travelconquestbe.api.chat.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.sparta.travelconquestbe.api.chat.dto.request.ChatRoomCreateRequest;
import com.sparta.travelconquestbe.api.chat.dto.respones.ChatRoomCreateResponse;
import com.sparta.travelconquestbe.api.chat.dto.respones.ChatRoomSearchResponse;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.chat.entity.ChatRoom;
import com.sparta.travelconquestbe.domain.chat.repository.ChatRoomRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

	private final ChatRoomRepository chatRoomRepository;

	// 채팅방 생성
	public ChatRoomCreateResponse createRoom(ChatRoomCreateRequest request) {
		ChatRoom chatRoom = ChatRoom.builder()
			.title(request.getTitle())
			.maxUsers(request.getMaxUsers())
			.password(request.getPassword())
			.build();
		ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);
		return ChatRoomCreateResponse.fromChatRoom(savedChatRoom);
	}

	public List<ChatRoomSearchResponse> searchAllRooms() {
		return chatRoomRepository.findAll().stream()
			.map(chatRoom -> new ChatRoomSearchResponse(
				chatRoom.getId(),
				chatRoom.getTitle(),
				chatRoom.getMaxUsers(),
				chatRoom.getCurrentUsers(),
				chatRoom.isHasPassword()))
			.toList();
	}

	public void enterChatRoom(Long chatRoomId) {
		ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
			.orElseThrow(() -> new CustomException("CHAT#1_002", "해당 채팅방이 존재하지 않습니다.", HttpStatus.NOT_FOUND));

		if (chatRoom.getCurrentUsers() >= chatRoom.getMaxUsers()) {
			throw new CustomException("CHAT#4_001", "입장 가능 인원이 초과되었습니다.", HttpStatus.FORBIDDEN);
		}

		chatRoom.addUser();
		System.out.println("ENTER current users :" + chatRoom.getCurrentUsers() );
		chatRoomRepository.save(chatRoom);
	}

	// 채팅방 퇴장 로직
	public void exitChatRoom(Long chatRoomId) {
		ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
			.orElseThrow(() -> new CustomException("CHAT#1_003", "해당 채팅방이 존재하지 않습니다.", HttpStatus.NOT_FOUND));
		chatRoom.removeUser();
		System.out.println("EXIT current users :" + chatRoom.getCurrentUsers() );
		chatRoomRepository.save(chatRoom);
	}
}