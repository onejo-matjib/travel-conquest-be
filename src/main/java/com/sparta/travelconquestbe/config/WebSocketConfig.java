package com.sparta.travelconquestbe.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

	private final WebSocketHandler webSocketHandler;

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry
			.addHandler(webSocketHandler, "/ws/conn")// /ws/conn 경로로 WebSocket 연결을 허용
			.setAllowedOrigins("*"); //CORS 허용 설정

	}
	//---------------------------------------------------------------------
	// CORS : 교차 출처 리소스 공유
	// 도메인이 다른 서버끼리 리소스를 주고 받을 때 보안을 위해 설정된 정책
	// 추가적으로 공부해서 블로그 남기기
	// 개발 완료시 지우기
	//---------------------------------------------------------------------
}
