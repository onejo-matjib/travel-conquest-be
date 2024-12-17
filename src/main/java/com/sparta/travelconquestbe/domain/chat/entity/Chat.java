package com.sparta.travelconquestbe.domain.chat.entity;

import java.time.LocalDateTime;

import com.sparta.travelconquestbe.common.entity.TimeStampCreated;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "chats")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chat extends TimeStampCreated {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom; // 채팅방과의 관계

    @Column(nullable = false)
    private String nickname; // 작성자 닉네임

    @Column(nullable = false)
    private String message; // 메시지 내용
}