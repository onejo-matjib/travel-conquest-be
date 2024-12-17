package com.sparta.travelconquestbe.api.notification.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class NotificationSerivce {
	public SseEmitter subscribe(Long id, String lastEventId) {
		return null;
	}
}
