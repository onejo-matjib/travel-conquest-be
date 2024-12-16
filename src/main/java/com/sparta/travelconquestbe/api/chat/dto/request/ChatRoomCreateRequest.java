package com.sparta.travelconquestbe.api.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomCreateRequest {
	@NotBlank(message = "채팅방 제목은 필수입니다.")
	@Size(max = 20, message = "채팅방 제목은 20자를 초과할 수 없습니다.")
	private String title;

	@NotNull(message = "최대 사용자 수는 필수입니다.")
	@Positive(message = "최대 사용자 수는 1 이상이어야 합니다.")
	private Integer maxUsers;

	private String password;
}
