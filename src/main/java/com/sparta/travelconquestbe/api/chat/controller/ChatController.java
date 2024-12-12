package com.sparta.travelconquestbe.api.chat.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.sparta.travelconquestbe.api.chat.service.ChatService;
import com.sparta.travelconquestbe.domain.chat.entity.Chat;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class ChatController {

	private final SimpMessagingTemplate messagingTemplate;
	private final ChatService chatService;

	// 메시지 받기 및 다른 클라이언트에게 전달하기
	@MessageMapping("/chat/{roomId}")
	// @SendTo("/sub/chat/{roomId}")
	public void sendMessage(@DestinationVariable Long roomId, Chat message) {
		// DB 저장
		chatService.sendMessage(roomId, message);

		// 클라이언트에게 메시지 전달
		// messagingTemplate.convertAndSend("/sub/chat/" + roomId, message);
		// return message;
	}


	// 직접 메시지를 특정 채팅방에 전송하는 메소드
	public void sendMessageToRoom(String roomId, Chat message) {
		messagingTemplate.convertAndSend("/sub/chat/" + roomId, message);
	}
}
