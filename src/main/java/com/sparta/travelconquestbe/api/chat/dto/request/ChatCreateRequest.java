package com.sparta.travelconquestbe.api.chat.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatCreateRequest {
	@NotBlank(message = "채팅방 제목은 필수입니다.")
	private String title;

	@Min(value = 1, message = "최대 참가자 수는 1명 이상이어야 합니다.")
	@Max(value = 100, message = "최대 참가자 수는 100명을 초과할 수 없습니다.")
	private int maxPlayers;
}
