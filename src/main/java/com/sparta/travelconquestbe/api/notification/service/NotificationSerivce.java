package com.sparta.travelconquestbe.api.notification.service;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.sparta.travelconquestbe.domain.notification.repository.EmitterRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class NotificationSerivce {

	private final EmitterRepository emitterRepository;
	private Long timeout = 60L * 1000L * 60L;

	public SseEmitter subscribe(Long userId, String lastEventId) {
		String emitterId = makeTimeIncludeId(userId);
		SseEmitter emitter = emitterRepository.save(emitterId, new SseEmitter(timeout));
		emitter.onCompletion(()->emitterRepository.deleteById(emitterId));
		emitter.onTimeout(()-> emitterRepository.deleteById(emitterId));

		//503 에러를 방지하기 위한 더미 이벤트 전송
		String eventId = makeTimeIncludeId(userId);
		sendNotification(emitter, eventId, emitterId, "EventStream Created. [userId=" + userId + "]");

		// 클라이언트가 미수신한 Event 목록이 존재할 경우 전송하여 Event 유실을 예방
		if(hasLostData(lastEventId)) {
			sendLostData(lastEventId, userId, emitterId, emitter);
		}

		return emitter;
	}

	private String makeTimeIncludeId(Long userId) {
		return userId + "_" + System.currentTimeMillis();
	}

	private void sendNotification(SseEmitter emitter, String eventId, String emitterId, Object data) {
		try {
			emitter.send(SseEmitter.event()
				.id(eventId)
				.data(data));
		} catch (IOException exception) {
			emitterRepository.deleteById(emitterId);
		}
	}

	private boolean hasLostData(String lastEventId) {
		return !lastEventId.isEmpty();
	}

	private void sendLostData(String lastEventId, Long userId, String emitterId, SseEmitter emitter) {
		Map<String, Object> eventCaches = emitterRepository.findAllEventCacheStartWithByUserId(String.valueOf(userId));
		eventCaches.entrySet().stream()
			.filter(entry -> lastEventId.compareTo(entry.getKey()) < 0)
			.forEach(entry -> sendNotification(emitter, entry.getKey(), emitterId, entry.getValue()));
	}
}
