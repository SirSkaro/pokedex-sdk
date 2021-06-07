package skaro.pokedex.sdk.messaging.cache;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import skaro.pokedex.sdk.cache.DiscordGuildCacheEvictionMessage;
import skaro.pokedex.sdk.client.guild.GuildSettings;

@ExtendWith(SpringExtension.class)
public class CacheManagerEvictMessageReceiverIntegrationTest {

	private CacheManager cacheManager;
	private CacheManagerEvictMessageReceiver receiver;
	
	@BeforeEach
	public void setup() {
		SimpleCacheManager simpleCacheManager = new SimpleCacheManager();
		Cache guildSettingsCache = new ConcurrentMapCache(GuildSettings.class.getName());
		simpleCacheManager.setCaches(List.of(guildSettingsCache));
		simpleCacheManager.initializeCaches();
		
		cacheManager = simpleCacheManager;
		receiver = new CacheManagerEvictMessageReceiver(cacheManager);
	}
	
	@Test
	public void testEvict_entityCached() {
		String guildId = UUID.randomUUID().toString();
		addSettingsToCache(guildId);
		
		assertNotNull(getCache().get(guildId, GuildSettings.class));
		receiver.evict(createEvictionMessage(guildId));
		assertNull(getCache().get(guildId, GuildSettings.class));
	}
	
	private void addSettingsToCache(String guildId) {
		GuildSettings settings = new GuildSettings();
		getCache().put(guildId, settings);
	}
	
	private DiscordGuildCacheEvictionMessage createEvictionMessage(String guildId) {
		GuildSettingsInvalidationMessage message = new GuildSettingsInvalidationMessage();
		message.setGuildId(guildId);
		return message;
	}
	
	private Cache getCache() {
		return cacheManager.getCache(GuildSettings.class.getName());
	}
	
}
