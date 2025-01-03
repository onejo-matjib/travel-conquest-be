package com.sparta.travelconquestbe.api.chat.dto.respones;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomExitResponse {
	private boolean success;
	private String message;
}
