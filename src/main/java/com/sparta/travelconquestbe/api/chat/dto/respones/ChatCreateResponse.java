package com.sparta.travelconquestbe.api.chat.dto.respones;

import com.sparta.travelconquestbe.domain.chat.entity.ChatRoom;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatCreateResponse {
	private Long id;
	private String title;
	private int maxPlayers;

	public ChatCreateResponse(ChatRoom chatRoom) {
		this.id = chatRoom.getId();
		this.title = chatRoom.getTitle();
		this.maxPlayers = chatRoom.getMaxPlayers();
	}
}