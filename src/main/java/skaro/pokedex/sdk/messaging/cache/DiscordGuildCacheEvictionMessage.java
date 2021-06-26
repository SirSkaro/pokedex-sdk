package skaro.pokedex.sdk.messaging.cache;

public interface DiscordGuildCacheEvictionMessage {
	String getGuildId();
	Class<?> getEntityClass();
}
