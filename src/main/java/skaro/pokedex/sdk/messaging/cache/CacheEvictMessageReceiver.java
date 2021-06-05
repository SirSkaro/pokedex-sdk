package skaro.pokedex.sdk.messaging.cache;

import skaro.pokedex.sdk.cache.DiscordGuildCacheEvictionMessage;

public interface CacheEvictMessageReceiver {
	public static final String RECEIVE_METHOD_NAME = "evict";

	<T> void evict(DiscordGuildCacheEvictionMessage guildAssociable);
	
}
