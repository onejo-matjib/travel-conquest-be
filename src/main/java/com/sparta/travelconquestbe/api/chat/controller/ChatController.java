package com.sparta.travelconquestbe.api.chat.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

import com.sparta.travelconquestbe.api.chat.dto.request.ChatSendMessageRequest;
import com.sparta.travelconquestbe.api.chat.service.ChatService;
import com.sparta.travelconquestbe.domain.chat.entity.Chat;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class ChatController {

	private final SimpMessagingTemplate messagingTemplate;
	private final ChatService chatService;
	private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

	@MessageMapping("/chat/{roomId}")
	public void sendMessage(@DestinationVariable Long roomId, @Validated ChatSendMessageRequest request) {
		logger.info("메시지 전송 요청 - roomId: {}, message: {}", roomId, request);
		chatService.sendMessage(roomId, request);
	}

	public void sendMessageToRoom(Long roomId, Chat message) {
		logger.info("채팅방 메시지 전송 - roomId: {}, message: {}", roomId, message);
		messagingTemplate.convertAndSend("/sub/chat/" + roomId, message);
	}
}
