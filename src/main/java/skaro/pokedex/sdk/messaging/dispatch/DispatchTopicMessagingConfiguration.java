package skaro.pokedex.sdk.messaging.dispatch;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DispatchTopicMessagingConfiguration {
	public static final String DISPATCH_TOPIC_EXCHANGE_BEAN = "dispatchTopicExchangeBean";
	
	public static final String TOPIC = "skaro.pokedex.dispatch";
	public static final String COMMAND_ROUTING_PATTERN_PREFIX = "command";
	public static final String SIMPLE_COMMAND_ROUTING_PATTERN_PREFIX = COMMAND_ROUTING_PATTERN_PREFIX + ".simple";
	
	@Bean(DISPATCH_TOPIC_EXCHANGE_BEAN)
	public TopicExchange topicExchange() {
		return new TopicExchange(TOPIC);
	}

}
