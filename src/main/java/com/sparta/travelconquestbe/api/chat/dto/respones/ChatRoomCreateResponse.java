package com.sparta.travelconquestbe.api.chat.dto.respones;

import com.sparta.travelconquestbe.domain.chat.entity.ChatRoom;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomCreateResponse {
	private Long id;
	private String title;
	private int maxUsers;
	private int currentUsers;
	private boolean hasPassword;

	public static ChatRoomCreateResponse fromChatRoom(ChatRoom chatRoom) {
		return ChatRoomCreateResponse.builder()
			.id(chatRoom.getId())
			.title(chatRoom.getTitle())
			.maxUsers(chatRoom.getMaxUsers())
			.currentUsers(chatRoom.getCurrentUsers())
			.hasPassword(chatRoom.isHasPassword())
			.build();
	}
}