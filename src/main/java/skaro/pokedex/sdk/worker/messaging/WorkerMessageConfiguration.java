package skaro.pokedex.sdk.worker.messaging;

import static skaro.pokedex.sdk.messaging.DispatchTopicMessagingConfiguration.SIMPLE_COMMAND_ROUTING_PATTERN_PREFIX;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.Valid;

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
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.GenericApplicationContext;

import skaro.pokedex.sdk.messaging.DispatchTopicMessagingConfiguration;
import skaro.pokedex.sdk.messaging.MessageReceiver;
import skaro.pokedex.sdk.worker.CommandRegistration;
import skaro.pokedex.sdk.worker.WorkerCommandConfigurationProperties;

@Configuration
@Import(DispatchTopicMessagingConfiguration.class)
public class WorkerMessageConfiguration {

	@Bean
	@Valid
	@ConfigurationProperties(WorkerCommandConfigurationProperties.WORKER_PROPERTIES_PREFIX)
	public WorkerCommandConfigurationProperties workerConfigurationProperties() {
		return new WorkerCommandConfigurationProperties();
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
	public Queue queue(WorkerCommandConfigurationProperties workerProperties) {
		return new AnonymousQueue();
	}
	
	@Bean 
	public List<Binding> bindings(GenericApplicationContext context, 
			Queue queue, 
			TopicExchange topic,
			WorkerCommandConfigurationProperties configProperties) {
		
		return configProperties.getCommands().stream()
				.flatMap(commandRegistration -> createBindings(commandRegistration, queue, topic))
				.map(binding -> registerBinding(context, binding))
				.collect(Collectors.toList());
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
	
	private Stream<Binding> createBindings(CommandRegistration registration, Queue queue, TopicExchange topic) {
		Stream<Binding> mainNameBinding = Stream.of(createBinding(queue, topic, registration.getName()));
		Stream<Binding> aliasBindings = registration.getAliases().stream()
				.map(alias -> createBinding(queue, topic, alias));
		
		return Stream.concat(mainNameBinding, aliasBindings);
	}
	
	private Binding createBinding(Queue queue, TopicExchange topic, String key) {
		return BindingBuilder.bind(queue)
				.to(topic)
				.with(SIMPLE_COMMAND_ROUTING_PATTERN_PREFIX + "." + key);
	}
	
	private Binding registerBinding(GenericApplicationContext context, Binding binding) {
		context.registerBean(binding.getRoutingKey() + "Binding", Binding.class, () -> binding);
		return binding;
	}
	
}
