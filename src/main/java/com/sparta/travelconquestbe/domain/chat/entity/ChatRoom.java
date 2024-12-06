package com.sparta.travelconquestbe.domain.chat.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chatrooms")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String title;
	private int maxPlayers;

	@Builder
	public ChatRoom(String title, int maxPlayers) {
		this.title = title;
		this.maxPlayers = maxPlayers;
	}
}