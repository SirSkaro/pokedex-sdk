package skaro.pokedex.sdk.messaging;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DispatchTopicMessagingConfiguration {
	public static final String TOPIC = "skaro.pokedex";
	public static final String COMMAND_ROUTING_PATTERN_PREFIX = "command";
	public static final String SIMPLE_COMMAND_ROUTING_PATTERN_PREFIX = COMMAND_ROUTING_PATTERN_PREFIX + ".simple";
	
	@Bean
	public TopicExchange topicExchange() {
		return new TopicExchange(TOPIC);
	}

}
