package skaro.pokedex.sdk.client;

import java.lang.invoke.MethodHandles;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;

import reactor.core.publisher.Mono;

public class MonoCacheFacade implements CacheFacade {
	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private CacheManager cacheManager;
	
	public MonoCacheFacade(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	@Override
	public <T> Mono<T> get(Class<T> cls, String key) {
		String cacheName = getCacheName(cls);
		Optional<T> cachedData = Optional.ofNullable(cacheManager.getCache(cacheName))
				.map(cache -> cache.get(key, cls));
		
		return Mono.justOrEmpty(cachedData);
	}

	@Override
	public <T> T cache(String key, T entity) {
		String cacheName = getCacheName(entity.getClass());
		Optional.ofNullable(cacheManager.getCache(cacheName))
			.ifPresentOrElse(
					cache -> cache.put(key, entity), 
					logCacheFailure(cacheName)
			);
		
		return entity;
	}
	
	private <T> String getCacheName(Class<T> type) {
		return type.getName();
	}
	
	private Runnable logCacheFailure(String cacheName) {
		return () -> LOG.warn("Unable to cache entity. Cache {} does not exist", cacheName);
	}

}
