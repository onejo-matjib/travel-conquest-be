package com.sparta.travelconquestbe.api.chat.dto.respones;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomEnterResponse {
	private boolean success;
	private String message;
}
