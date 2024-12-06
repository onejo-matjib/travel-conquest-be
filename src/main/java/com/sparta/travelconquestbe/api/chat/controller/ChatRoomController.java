package com.sparta.travelconquestbe.api.chat.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.travelconquestbe.api.chat.dto.request.ChatCreateRequest;
import com.sparta.travelconquestbe.api.chat.dto.respones.ChatCreateResponse;
import com.sparta.travelconquestbe.api.chat.dto.respones.ChatRoomSearchResponse;
import com.sparta.travelconquestbe.api.chat.service.ChatRoomService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat/rooms")
public class ChatRoomController {

	private final ChatRoomService chatRoomService;

	@PostMapping
	public ResponseEntity<ChatCreateResponse> createChatRoom(
		@Valid @RequestBody ChatCreateRequest chatCreateRequest) {
		ChatCreateResponse response = chatRoomService.createChatRoom(chatCreateRequest);
		return ResponseEntity.ok(response);
	}

	@GetMapping
	public ResponseEntity<List<ChatRoomSearchResponse>> getChatRooms() {
		List<ChatRoomSearchResponse> response = chatRoomService.searchChatRooms();
		return ResponseEntity.ok(response);
	}
}

