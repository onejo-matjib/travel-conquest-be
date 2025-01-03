package com.sparta.travelconquestbe.api.chat.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.travelconquestbe.api.chat.dto.request.ChatRoomCreateRequest;
import com.sparta.travelconquestbe.api.chat.dto.respones.ChatRoomCreateResponse;
import com.sparta.travelconquestbe.api.chat.dto.respones.ChatRoomEnterResponse;
import com.sparta.travelconquestbe.api.chat.dto.respones.ChatRoomExitResponse;
import com.sparta.travelconquestbe.api.chat.dto.respones.ChatRoomSearchResponse;
import com.sparta.travelconquestbe.api.chat.service.ChatRoomService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat/rooms")
public class ChatRoomController {

	private final ChatRoomService chatRoomService;

	// 채팅방 생성
	@PostMapping
	public ResponseEntity<ChatRoomCreateResponse> createRoom(@Valid @RequestBody ChatRoomCreateRequest request) {
		ChatRoomCreateResponse response = chatRoomService.createRoom(request);
		return ResponseEntity.ok(response);
	}

	@GetMapping
	public ResponseEntity<List<ChatRoomSearchResponse>> searchAllChatRooms() {
		List<ChatRoomSearchResponse> responses = chatRoomService.searchAllRooms();
		return ResponseEntity.ok(responses);
	}

	// 채팅방 입장 처리
	@MessageMapping("/enter/{chatRoomId}")
	@SendTo("/sub/chatroom/{chatRoomId}")
	public ChatRoomEnterResponse enterChatRoom(@DestinationVariable Long chatRoomId) {
		chatRoomService.enterChatRoom(chatRoomId);
		return new ChatRoomEnterResponse(true, "채팅방에 입장했습니다.");
	}

	// 채팅방 퇴장 처리
	@MessageMapping("/exit/{chatRoomId}")  // /pub/exit/{chatRoomId}로 요청이 들어오면 이 메서드가 호출됩니다.
	@SendTo("/sub/chatroom/{chatRoomId}")  // /sub/chatroom/{chatRoomId}로 메시지가 발송됩니다.
	public ChatRoomExitResponse exitChatRoom(@DestinationVariable Long chatRoomId) {
		chatRoomService.exitChatRoom(chatRoomId);
		return new ChatRoomExitResponse(true, "채팅방에서 퇴장했습니다.");
	}
}