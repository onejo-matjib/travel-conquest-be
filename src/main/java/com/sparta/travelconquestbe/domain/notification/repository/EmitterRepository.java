package com.sparta.travelconquestbe.domain.notification.repository;

import java.util.Map;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface EmitterRepository {
	SseEmitter save(String emitterId, SseEmitter sseEmitter);
	void saveEventCache(String emitterId, Object event);
	Map<String, SseEmitter> findAllEmitterStartWithByUserId(String userId);
	Map<String, Object> findAllEventCacheStartWithByUserId(String userId);
	void deleteById(String id);
	void deleteAllEmitterStartWithId(String userId);
	void deleteAllEventCacheStartWithId(String userId);
}

// Emitter Repository 필요성 :
// SSE Emitter를 이용해 알림을 보내게 되는데 회원에게 어떤 Emiiter가 연결되어있는지를
// 저장해줘야하고 어떤 이벤트들이 현재까지 발생했는지에 대해서도 저장하고 있어야 한다.
// (추후 Emitter의 연결이 끊기게 되면 저장되어 있는 Event를 기반으로 이를 전송해야 하기 때문)

// save - Emitter 저장
// saveEventCache - 이벤트 저장
// findAllEmitterStartWithByUserId - 해당 회원과 관련된 모든 Emitter를 찾는다.
// findAllEventCacheStartWithByUserId - 해당 회원과 관련된 모든 이벤트를 찾는다.
// deleteById - Emitter를 지운다
// deleteAllEmitterStartWithId - 해당 회원과 관련된 모든 Emitter를 지운다.
// deleteAllEventCacheStartWithId - 해당 회원과 관련된 모든 이벤트를 지운다.

// id 값으로 String 사용이유 : emitterId = userId + "-" + currentTimeMillis를
// 사용하기 때문에 다양한 형식의 ID 처리를 위해 사용한다.
// 저러한 형태로 사용하는 이유는 충돌 방지를 위해 고유 식별자로 생성하기 위함이다
// 추가적인 이유로 검색 편의를 위한 접두사 검색을 findAllStartWith 작업에 유리하다
