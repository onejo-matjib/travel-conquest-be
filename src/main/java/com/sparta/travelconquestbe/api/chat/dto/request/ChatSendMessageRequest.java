package com.sparta.travelconquestbe.api.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatSendMessageRequest {
	@NotBlank(message = "닉네임은 필수입니다.")
	private String nickname;

	@NotBlank(message = "메시지는 비어 있을 수 없습니다.")
	@Size(max = 100, message = "메시지는 100자를 초과할 수 없습니다.")
	private String message;
}
