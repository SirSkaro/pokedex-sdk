package skaro.pokedex.sdk.messaging.gateway;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayMessagingConfiguration {
	public static final String DISCORD_MESSAGE_EVENT_QUEUE = "discordMessageEvent";
	public static final String GATEWAY_QUEUE_BEAN = "gatewayQueue";
	
	@Bean(GATEWAY_QUEUE_BEAN)
    public Queue discordMessageEventQueue() {
        return new Queue(DISCORD_MESSAGE_EVENT_QUEUE);
    }
	
}
