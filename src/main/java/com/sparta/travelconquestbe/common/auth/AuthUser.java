package com.sparta.travelconquestbe.common.auth;

import lombok.Getter;
// 임시 생성 by 홍주영
@Getter
public class AuthUser {
	private final Long userId;

	public AuthUser(Long userId) {
		this.userId = userId;
	}
}
