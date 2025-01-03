package com.sparta.travelconquestbe.api.chat.dto.respones;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatRoomSearchResponse {
	private Long id;
	private String title;
	private int maxUsers;
	private int currentUsers;
	private boolean hasPassword;
}