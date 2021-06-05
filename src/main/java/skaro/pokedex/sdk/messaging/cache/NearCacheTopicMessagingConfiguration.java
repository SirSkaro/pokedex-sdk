package skaro.pokedex.sdk.messaging.cache;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NearCacheTopicMessagingConfiguration {
	public static final String NEAR_CACHE_TOPIC_EXCHANGE_BEAN = "cacheTopicExchangeBean";
	
	public static final String TOPIC = "skaro.pokedex.cache";
	public static final String DISCORD_GUILD_ROUTING_PATTERN_PREFIX = "discord.guild";
	public static final String DISCORD_GUILD_SETTINGS_ROUTING_PATTERN_PREFIX = DISCORD_GUILD_ROUTING_PATTERN_PREFIX + ".settings";
	public static final String DISCORD_RATE_LIMIT_ROUTING_PATTERN_PREFIX = DISCORD_GUILD_ROUTING_PATTERN_PREFIX + ".limit";
	
	@Bean(NEAR_CACHE_TOPIC_EXCHANGE_BEAN)
	public TopicExchange topicExchange() {
		return new TopicExchange(TOPIC);
	}
}
