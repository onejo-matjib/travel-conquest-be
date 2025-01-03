package com.sparta.travelconquestbe.domain.notification.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.sparta.travelconquestbe.domain.notification.entity.Notification;
import com.sparta.travelconquestbe.domain.notification.enums.NotificationType;
import com.sparta.travelconquestbe.domain.user.entity.User;
import com.sparta.travelconquestbe.domain.user.enums.UserType;

class EmitterRepositoryImplTest {

	private EmitterRepositoryImpl emitterRepository = new EmitterRepositoryImpl();
	private Long DEFAULT_TIMEOUT = 60L * 1000L * 60L;


	@Test
	@DisplayName("새로운 Emitter를 추가한다.")
	void save() {
		//given
		long userId = 1L;
		String emitterId = userId + "_" + System.currentTimeMillis();
		SseEmitter sseEmitter = new SseEmitter(DEFAULT_TIMEOUT);

		//when, then
		Assertions.assertDoesNotThrow(() -> emitterRepository.save(emitterId, sseEmitter));
	}

	@Test
	@DisplayName("수신한 이벤트를 캐시에 저장한다.")
	void saveEventCache() {
		//given
		long userId = 1L;
		String eventCacheId = userId + "_" + System.currentTimeMillis();

		User user = User.builder()
			.id(userId) // 테스트용 ID 설정
			.name("홍길동")
			.nickname("길동이")
			.email("gildong@test.com")
			.password("testPassword123")
			.birth("19940101")
			.type(UserType.USER)
			.build();

		Notification notification = Notification.builder()
			.user(user)
			.notificationType(NotificationType.TEST)
			.message("새로운 테스트 알림이 있습니다.")
			.build();

		//when, then
		Assertions.assertDoesNotThrow(()-> emitterRepository.saveEventCache(eventCacheId, notification));
	}

	@Test
	@DisplayName("임의의 회원이 접속한 모든 Emitter 찾기")
	void findAllEmitterStartWithByUserId() throws Exception{
		//given
		Long userId = 1L;
		String emitterId1 = userId + "_" + System.currentTimeMillis();
		emitterRepository.save(emitterId1, new SseEmitter(DEFAULT_TIMEOUT));

		Thread.sleep(100);
		String emitterId2 = userId + "_" + System.currentTimeMillis();
		emitterRepository.save(emitterId2, new SseEmitter(DEFAULT_TIMEOUT));

		Thread.sleep(100);
		String emitterId3 = userId + "_" + System.currentTimeMillis();
		emitterRepository.save(emitterId3, new SseEmitter(DEFAULT_TIMEOUT));

		//when
		Map<String, SseEmitter> ActualResult = emitterRepository.findAllEmitterStartWithByUserId(String.valueOf(userId));

		//then
		Assertions.assertEquals(3, ActualResult.size());
	}

	@Test
	@DisplayName("어떤 회원에게 수신된 이벤트를 캐시에서 모두 찾는다.")
	void findAllEventCacheStartWithByUserId() throws Exception {
		//given
		Long userId = 1L;
		User user = User.builder()
			.id(userId) // 테스트용 ID 설정
			.name("홍길동")
			.nickname("길동이")
			.email("gildong@test.com")
			.password("testPassword123")
			.birth("19940101")
			.type(UserType.USER)
			.build();

		String eventCacheId1 =  userId + "_" + System.currentTimeMillis();
		Notification notification1 = Notification.builder()
			.user(user)
			.notificationType(NotificationType.TEST)
			.message("새로운 테스트 알림이 있습니다.")
			.build();
		emitterRepository.saveEventCache(eventCacheId1, notification1);

		Thread.sleep(100);
		String eventCacheId2 =  userId + "_" + System.currentTimeMillis();
		Notification notification2 = Notification.builder()
			.user(user)
			.notificationType(NotificationType.TEST)
			.message("새로운 테스트 알림이 있습니다.")
			.build();
		emitterRepository.saveEventCache(eventCacheId2, notification2);

		Thread.sleep(100);
		String eventCacheId3 =  userId + "_" + System.currentTimeMillis();
		Notification notification3 = Notification.builder()
			.user(user)
			.notificationType(NotificationType.TEST)
			.message("새로운 테스트 알림이 있습니다.")
			.build();
		emitterRepository.saveEventCache(eventCacheId3, notification3);

		//when
		Map<String, Object> ActualResult = emitterRepository.findAllEventCacheStartWithByUserId(String.valueOf(userId));

		//then
		Assertions.assertEquals(3, ActualResult.size());
	}

	@Test
	@DisplayName("ID를 통해 Emitter를 Repository에서 제거한다.")
	void deleteById() {
		//given
		Long userId = 1L;
		String emitterId =  userId + "_" + System.currentTimeMillis();
		SseEmitter sseEmitter = new SseEmitter(DEFAULT_TIMEOUT);

		//when
		emitterRepository.save(emitterId, sseEmitter);
		emitterRepository.deleteById(emitterId);

		//then
		Assertions.assertEquals(0, emitterRepository.findAllEmitterStartWithByUserId(emitterId).size());
	}

	@Test
	@DisplayName("저장된 모든 Emitter를 제거한다.")
	void deleteAllEmitterStartWithId() throws Exception{
		//given
		Long userId = 1L;
		String emitterId1 = userId + "_" + System.currentTimeMillis();
		emitterRepository.save(emitterId1, new SseEmitter(DEFAULT_TIMEOUT));

		Thread.sleep(100);
		String emitterId2 = userId + "_" + System.currentTimeMillis();
		emitterRepository.save(emitterId2, new SseEmitter(DEFAULT_TIMEOUT));

		//when
		emitterRepository.deleteAllEmitterStartWithId(String.valueOf(userId));

		//then
		Assertions.assertEquals(0, emitterRepository.findAllEmitterStartWithByUserId(String.valueOf(userId)).size());
	}


	@Test
	@DisplayName("저장된 모든 이벤트를 제거한다")
	void deleteAllEventCacheStartWithId() throws Exception {
		//given
		Long userId = 1L;
		User user = User.builder()
			.id(userId) // 테스트용 ID 설정
			.name("홍길동")
			.nickname("길동이")
			.email("gildong@test.com")
			.password("testPassword123")
			.birth("19940101")
			.type(UserType.USER)
			.build();

		String eventCacheId1 =  userId + "_" + System.currentTimeMillis();
		Notification notification1 = Notification.builder()
			.user(user)
			.notificationType(NotificationType.TEST)
			.message("새로운 테스트 알림이 있습니다.")
			.build();

		emitterRepository.saveEventCache(eventCacheId1, notification1);

		Thread.sleep(100);
		String eventCacheId2 =  userId + "_" + System.currentTimeMillis();
		Notification notification2 = Notification.builder()
			.user(user)
			.notificationType(NotificationType.TEST)
			.message("새로운 테스트 알림이 있습니다.")
			.build();
		emitterRepository.saveEventCache(eventCacheId2, notification2);
		//when
		emitterRepository.deleteAllEventCacheStartWithId(String.valueOf(userId));
		//then
		Assertions.assertEquals(0, emitterRepository.findAllEventCacheStartWithByUserId(String.valueOf(userId)).size());
	}
}