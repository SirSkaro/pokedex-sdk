package skaro.pokedex.sdk;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayMessagingConfiguration {
	public static final String DISCORD_MESSAGE_EVENT_QUEUE = "discordMessageEvent";
	
	@Bean
    public Queue discordMessageEventQueue() {
        return new Queue(DISCORD_MESSAGE_EVENT_QUEUE);
    }
	
}
