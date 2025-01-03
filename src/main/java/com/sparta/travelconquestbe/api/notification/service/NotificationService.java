package com.sparta.travelconquestbe.api.notification.service;

import com.sparta.travelconquestbe.domain.notification.entity.Notification;
import com.sparta.travelconquestbe.domain.notification.enums.NotificationType;
import com.sparta.travelconquestbe.domain.notification.repository.NotificationRepository;
import com.sparta.travelconquestbe.domain.user.entity.User;
import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.sparta.travelconquestbe.domain.notification.repository.EmitterRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class NotificationService {

	private final EmitterRepository emitterRepository;
	private static final Long DEFAULT_TIMEOUT = 60L * 1000L * 60L;
	private final NotificationRepository notificationRepository;

	public SseEmitter subscribe(Long userId, String lastEventId) {
		String emitterId = makeTimeIncludeId(userId);
		SseEmitter emitter = emitterRepository.save(emitterId, new SseEmitter(DEFAULT_TIMEOUT));
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

	@Transactional
	public void notifyUserSuspended(User user, int suspensionDays) {
		String message = "안녕하세요, " + user.getName() + "님.\n" +
				"귀하의 계정이 " + suspensionDays + "일 동안 정지되었습니다.\n" +
				"문의 사항이 있으시면 관리자에게 연락해주세요.";

		Notification notification = Notification.builder()
				.message(message)
				.notificationType(NotificationType.SUSPENSION)
				.user(user)
				.build();
		notificationRepository.save(notification);
	}

	@Transactional
	public void notifySuspensionLifted(User user) {
		String message = "귀하의 계정 정지가 해제되었습니다. 다시 로그인할 수 있습니다.";
		Notification notification = Notification.builder()
				.message(message)
				.notificationType(NotificationType.SUSPENSION_LIFTED)
				.user(user)
				.build();
		notificationRepository.save(notification);
	}
}
