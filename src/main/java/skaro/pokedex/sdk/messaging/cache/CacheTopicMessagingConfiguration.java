package skaro.pokedex.sdk.messaging.cache;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheTopicMessagingConfiguration {
	public static final String CACHE_TOPIC_EXCHANGE_BEAN = "cacheTopicExchangeBean";
	
	public static final String TOPIC = "skaro.pokedex.cache";
	public static final String DISCORD_GUILD_ROUTING_PATTERN_PREFIX = "discord.guild";
	public static final String DISCORD_PREFIX_ROUTING_PATTERN_PREFIX = DISCORD_GUILD_ROUTING_PATTERN_PREFIX + ".prefix";
	public static final String DISCORD_RATE_LIMIT_ROUTING_PATTERN_PREFIX = DISCORD_GUILD_ROUTING_PATTERN_PREFIX + ".limit";
	public static final String DISCORD_LANGUAGE_ROUTING_PATTERN_PREFIX = DISCORD_GUILD_ROUTING_PATTERN_PREFIX + ".language";
	
	@Bean(CACHE_TOPIC_EXCHANGE_BEAN)
	public TopicExchange topicExchange() {
		return new TopicExchange(TOPIC);
	}
}
