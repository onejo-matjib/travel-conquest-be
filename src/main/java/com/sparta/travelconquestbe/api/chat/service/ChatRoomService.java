package com.sparta.travelconquestbe.api.chat.service;

import static org.springframework.http.HttpStatus.*;

import java.util.List;

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


	// 채팅방 입장 로직
	public void enterChatRoom(Long chatRoomId, Long userId) {
		// 1. 채팅방이 존재하는지 확인
		ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
			.orElseThrow(() -> new RuntimeException("채팅방이 존재하지 않습니다."));

		// 2. 채팅방 입장 시 현재 사용자 수 증가
		chatRoom.setCurrentUsers(chatRoom.getCurrentUsers() + 1);

		// 3. 변경된 채팅방 데이터를 저장
		chatRoomRepository.save(chatRoom);
	}

	// 채팅방 퇴장 로직
	public void exitChatRoom(Long chatRoomId, Long userId) {
		// 1. 채팅방이 존재하는지 확인
		ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
			.orElseThrow(() -> new RuntimeException("채팅방이 존재하지 않습니다."));

		// 2. 채팅방 퇴장 시 현재 사용자 수 감소
		if (chatRoom.getCurrentUsers() > 0) {
			chatRoom.setCurrentUsers(chatRoom.getCurrentUsers() - 1);
		} else {
			throw new RuntimeException("채팅방에 아무도 없습니다.");
		}

		// 3. 변경된 채팅방 데이터를 저장
		chatRoomRepository.save(chatRoom);
	}
}
