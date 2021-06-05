package skaro.pokedex.sdk.messaging.cache;

import java.lang.invoke.MethodHandles;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;

import skaro.pokedex.sdk.cache.DiscordGuildCacheEvictionMessage;

public class CacheManagerEvictMessageReceiver implements CacheEvictMessageReceiver {
	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private CacheManager cacheManager;
	
	public CacheManagerEvictMessageReceiver(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	@Override
	public <T> void evict(DiscordGuildCacheEvictionMessage guildAssociable) {
		LOG.info("Evicting {} data for guild {}", guildAssociable.getEntityClass().getSimpleName(), guildAssociable.getGuildId());
		
		Optional.of(cacheManager)
			.map(manager -> manager.getCache(getCacheName(guildAssociable)))
			.map(cache -> cache.evictIfPresent(guildAssociable.getGuildId()))
			.orElse(false);
	}
	
	private String getCacheName(DiscordGuildCacheEvictionMessage guildAssociable) {
		return guildAssociable.getEntityClass().getName();
	}

}
