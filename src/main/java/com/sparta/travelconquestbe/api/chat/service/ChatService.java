package com.sparta.travelconquestbe.api.chat.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.sparta.travelconquestbe.api.chat.dto.request.ChatSendMessageRequest;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.chat.entity.Chat;
import com.sparta.travelconquestbe.domain.chat.entity.ChatRoom;
import com.sparta.travelconquestbe.domain.chat.repository.ChatRepository;
import com.sparta.travelconquestbe.domain.chat.repository.ChatRoomRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatService {

	private final ChatRepository chatRepository;
	private final SimpMessagingTemplate messagingTemplate;
	private final ChatRoomRepository chatRoomRepository;
	private static final Logger logger = LoggerFactory.getLogger(ChatService.class);


	public Chat sendMessage(Long roomId, ChatSendMessageRequest request) throws CustomException {
		logger.info("채팅 메시지 저장 및 전송 시작 - roomId: {}", roomId);

		ChatRoom chatRoom = chatRoomRepository.findById(roomId)
			.orElseThrow(() -> new CustomException("CHAT#1_001", "해당 채팅방이 존재하지 않습니다.", HttpStatus.NOT_FOUND));

		Chat chat = Chat.builder()
			.chatRoom(chatRoom)
			.nickname(request.getNickname())
			.message(request.getMessage())
			.build();

		Chat savedChat = chatRepository.save(chat);

		messagingTemplate.convertAndSend("/sub/chat/" + roomId, savedChat);
		logger.info("채팅 메시지 저장 및 전송 완료 - roomId: {}", roomId);

		return savedChat;
	}
}
