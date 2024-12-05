package com.sparta.travelconquestbe.config;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketChatHandler extends TextWebSocketHandler {

	private final ObjectMapper mapper;

	//소켓 세션을 저장할 Set
	private final Set<WebSocketSession> sessions = new HashSet<>();

	//소켓 연결 확인
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		log.info("{}연결됨", session.getId());
		sessions.add(session);
		session.sendMessage(new TextMessage("WebSocket 연결 완료"));
	}

	//소켓 메세지 처리
	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		String payload = message.getPayload();
		log.info("임의의 사용자: {}", payload);

		for (WebSocketSession webSocketSession : sessions) {
			if (!webSocketSession.getId().equals(session.getId())) {  // 자기 자신을 제외
				webSocketSession.sendMessage(new TextMessage(payload));
			}
		}
	}

	//소켓 연결 종료
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		log.info("{} 연결 끊김", session.getId());
		sessions.remove(session);
	}
}
