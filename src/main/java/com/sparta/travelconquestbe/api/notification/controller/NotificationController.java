package com.sparta.travelconquestbe.api.notification.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.sparta.travelconquestbe.api.notification.service.NotificationSerivce;
import com.sparta.travelconquestbe.common.annotation.AuthUser;
import com.sparta.travelconquestbe.common.auth.AuthUserInfo;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

	private final NotificationSerivce notificationSerivce;


	@GetMapping(value = "/subscribe", produces = "text/event-stream")
	@ResponseStatus(HttpStatus.OK)
	public SseEmitter subscribe(
		@AuthUser AuthUserInfo user,
		@RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "")
		String lastEventId
	) {
		return notificationSerivce.subscribe(user.getId(), lastEventId);
	}
	// 실제 클라이언트로부터 오는 알림 구독 요청을 받는다.
	// 누구로부터 온 알림 구독인지는 AuthUserInfo 사용
	// 이전에 받지 못한 정보가 있다면, Last-Event-ID라는 헤더와 함께 날아옴 이에 대한 정보를 받는다
	// Last-Event-ID -> SSE 연결에 대한 시간만료 or 종료에 대하여 마지막 이벤트 ID를 활용 하여
	// 받지못한 데이터를 받게하기 위한 ID값



}
