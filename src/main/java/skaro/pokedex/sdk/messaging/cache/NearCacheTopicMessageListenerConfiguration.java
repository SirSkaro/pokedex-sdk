package skaro.pokedex.sdk.messaging.cache;

import static skaro.pokedex.sdk.messaging.cache.CacheEvictMessageReceiver.RECEIVE_METHOD_NAME;
import static skaro.pokedex.sdk.messaging.cache.NearCacheTopicMessagingConfiguration.DISCORD_GUILD_ROUTING_PATTERN_PREFIX;
import static skaro.pokedex.sdk.messaging.cache.NearCacheTopicMessagingConfiguration.NEAR_CACHE_TOPIC_EXCHANGE_BEAN;

import java.util.concurrent.Executor;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.AnonymousQueue;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.DirectMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(NearCacheTopicMessagingConfiguration.class)
public class NearCacheTopicMessageListenerConfiguration {
	public static final String GUILD_SETTINGS_CACHE_QUEUE_BEAN = "guildSettingsCacheQueue";
	public static final String CACHE_BINDING_BEAN = "cacheBinding";
	public static final String CACHE_MESSAGE_LISTENER_ADAPTER_BEAN = "cacheMessageListenerAdapter";
	
	@Bean(GUILD_SETTINGS_CACHE_QUEUE_BEAN)
	public Queue queue() {
		return new AnonymousQueue();
	}
	
	@Bean
	public MessageListenerContainer messageListenerContainer(ConnectionFactory connectionFactory, 
			@Qualifier(GUILD_SETTINGS_CACHE_QUEUE_BEAN) Queue queue, 
			@Qualifier(CACHE_MESSAGE_LISTENER_ADAPTER_BEAN) MessageListenerAdapter adapter,
			Executor executor) {
		DirectMessageListenerContainer listenerContainer = new DirectMessageListenerContainer();
		listenerContainer.setConnectionFactory(connectionFactory);
		listenerContainer.setAcknowledgeMode(AcknowledgeMode.NONE);
		listenerContainer.setQueues(queue);
		listenerContainer.setDefaultRequeueRejected(false);
		listenerContainer.setShutdownTimeout(100);
		listenerContainer.setMessageListener(adapter);
		listenerContainer.setTaskExecutor(executor);

		return listenerContainer;
	}
	
	@Bean(CACHE_BINDING_BEAN)
	public Binding binding(
			@Qualifier(GUILD_SETTINGS_CACHE_QUEUE_BEAN) Queue queue, 
			@Qualifier(NEAR_CACHE_TOPIC_EXCHANGE_BEAN) TopicExchange topic) {
		return BindingBuilder.bind(queue)
				.to(topic)
				.with(DISCORD_GUILD_ROUTING_PATTERN_PREFIX + ".#");
	}
	
	@Bean
	public CacheEvictMessageReceiver cacheEvictMessageReceiver(CacheManager manager) {
		return new CacheManagerEvictMessageReceiver(manager);
	}
	
	@Bean(CACHE_MESSAGE_LISTENER_ADAPTER_BEAN)
	public MessageListenerAdapter listenerAdapter(CacheEvictMessageReceiver receiver) {
		return new MessageListenerAdapter(receiver, RECEIVE_METHOD_NAME);
	}
	
}
