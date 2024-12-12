package com.sparta.travelconquestbe.api.chat.dto.respones;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatRoomSearchResponse {
	private Long id;            // 채팅방 ID
	private String title;       // 채팅방 제목
	private int maxUsers;       // 최대 사용자 수
	private int currentUsers;   // 현재 사용자 수
	private boolean hasPassword; // 비밀번호 사용 여부
}