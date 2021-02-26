package skaro.pokedex.sdk.worker.messaging;

import java.util.concurrent.Executor;

import javax.validation.Valid;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.DirectMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import skaro.pokedex.sdk.messaging.MessageReceiver;

@Configuration
public class WorkerMessageConfiguration {

	@Bean
	@Valid
	@ConfigurationProperties(WorkerConfigurationProperties.WORKER_PROPERTIES_PREFIX)
	public WorkerConfigurationProperties workerConfigurationProperties() {
		return new WorkerConfigurationProperties();
	}
	
	@Bean
	public MessageReceiver<WorkRequest> messageReceiver() {
		return new WorkRequestReceiver();
	}
	
	@Bean
	public MessageListenerAdapter listenerAdapter(WorkRequestReceiver receiver) {
		return new MessageListenerAdapter(receiver, MessageReceiver.RECIEVE_METHOD_NAME);
	}
	
	@Bean
	public Queue queue(WorkerConfigurationProperties workerProperties) {
		return new Queue(workerProperties.getQueue());
	}
	
	@Bean
	public MessageListenerContainer messageListenerContainer(ConnectionFactory connectionFactory, 
			Queue queue, 
			MessageListenerAdapter adapter,
			Executor executor) {
		DirectMessageListenerContainer listenerContainer = new DirectMessageListenerContainer();
		listenerContainer.setConnectionFactory(connectionFactory);
		listenerContainer.setAcknowledgeMode(AcknowledgeMode.NONE);
		listenerContainer.setQueues(queue);
		listenerContainer.setDefaultRequeueRejected(false);
		listenerContainer.setMessageListener(adapter);
		listenerContainer.setTaskExecutor(executor);

		return listenerContainer;
	}
	
}
