package com.sparta.travelconquestbe.api.chat.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomCreateRequest {
	private String title;
	private int maxUsers;
	private String password;
}
