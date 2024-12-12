package com.sparta.travelconquestbe.api.chat.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.sparta.travelconquestbe.domain.chat.entity.Chat;
import com.sparta.travelconquestbe.domain.chat.entity.ChatRoom;
import com.sparta.travelconquestbe.domain.chat.repository.ChatRepository;
import com.sparta.travelconquestbe.domain.chat.repository.ChatRoomRepository;

import lombok.RequiredArgsConstructor;

@Service
public class ChatService {

	private final ChatRepository chatRepository;
	private final SimpMessagingTemplate messagingTemplate;
	private final ChatRoomRepository chatRoomRepository;

	@Autowired
	public ChatService(ChatRepository chatRepository, ChatRoomRepository chatRoomRepository, SimpMessagingTemplate messagingTemplate) {
		this.chatRepository = chatRepository;
		this.messagingTemplate = messagingTemplate;
		this.chatRoomRepository = chatRoomRepository;
	}

	public Chat sendMessage(Long roomId, Chat message) {
		// 채팅방 ID와 메시지를 처리하는 로직

		// 채팅방 ID로 채팅방을 찾아서 채팅 메시지를 저장
		ChatRoom chatRoom = chatRoomRepository.findById(roomId)
			.orElseThrow(() -> new IllegalArgumentException("Invalid room ID"));

		// Chat 엔티티로 메시지 저장
		Chat chat = Chat.builder()
			.chatRoom(chatRoom)
			.nickname(message.getNickname())
			.message(message.getMessage())
			.build();

		chatRepository.save(chat); // DB에 메시지 저장

		// 메시지를 구독자에게 전송
		messagingTemplate.convertAndSend("/sub/chat/" + roomId, message);
		return chat;
	}
}
