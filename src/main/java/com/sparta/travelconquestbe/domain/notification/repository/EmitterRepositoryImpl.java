package com.sparta.travelconquestbe.domain.notification.repository;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.NoArgsConstructor;

@Repository
@NoArgsConstructor
public class EmitterRepositoryImpl implements EmitterRepository {

	private final Map<String, SseEmitter> emitters = new HashMap<>();
	private final Map<String, Object> eventCache = new HashMap<>();

	@Override
	public SseEmitter save(String emitterId, SseEmitter sseEmitter) {
		emitters.put(emitterId, sseEmitter);
		return sseEmitter;
	}

	@Override
	public void saveEventCache(String eventCacheId, Object event) {
		eventCache.put(eventCacheId, event);
	}

	@Override
	public Map<String, SseEmitter> findAllEmitterStartWithByUserId(String userId) {
		return emitters.entrySet().stream()
			.filter(entry -> entry.getKey().startsWith(userId))
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	@Override
	public Map<String, Object> findAllEventCacheStartWithByUserId(String userId) {
		return eventCache.entrySet().stream()
			.filter(entry -> entry.getKey().startsWith(userId))
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	@Override
	public void deleteById(String id) {
		emitters.remove(id);
	}

	@Override
	public void deleteAllEmitterStartWithId(String userId) {
		emitters.keySet().removeIf(key -> key.startsWith(userId));
	}

	@Override
	public void deleteAllEventCacheStartWithId(String userId) {
		Iterator<Map.Entry<String, Object>> iterator = eventCache.entrySet().iterator();

		while (iterator.hasNext()) {
			Map.Entry<String, Object> entry = iterator.next();
			if (entry.getKey().startsWith(userId)) {
				iterator.remove(); // 안전하게 제거
			}
		}
	}
}
