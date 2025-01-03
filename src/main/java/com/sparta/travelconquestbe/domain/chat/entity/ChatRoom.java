package com.sparta.travelconquestbe.domain.chat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "chatrooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title; // 채팅방 제목

	@Column(nullable = false)
	private int maxUsers; // 최대 사용자 수

	@Column
	private String password; // 비밀번호 (선택)

	@Column(nullable = false)
	private boolean hasPassword; // 비밀번호 사용 여부

	@Column(nullable = false)
	private int currentUsers = 0; // 현재 사용자 수 (기본값 0)

	@Builder
	public ChatRoom(String title, int maxUsers, String password) {
		this.title = title;
		this.maxUsers = maxUsers;
		this.password = password;
		this.hasPassword = (password != null && !password.isEmpty());
	}

	// 현재 사용자 증가
	public void addUser() {
		if (currentUsers >= maxUsers) {
			throw new IllegalStateException("채팅방 정원이 초과되었습니다.");
		}
		this.currentUsers++;
	}

	// 현재 사용자 감소
	public void removeUser() {
		if (currentUsers > 0) {
			this.currentUsers--;
		}
	}
}