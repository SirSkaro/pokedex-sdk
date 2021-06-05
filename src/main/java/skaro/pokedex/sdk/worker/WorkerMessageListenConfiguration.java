package skaro.pokedex.sdk.worker;

import static skaro.pokedex.sdk.messaging.dispatch.DispatchTopicMessagingConfiguration.DISPATCH_TOPIC_EXCHANGE_BEAN;
import static skaro.pokedex.sdk.messaging.dispatch.DispatchTopicMessagingConfiguration.SIMPLE_COMMAND_ROUTING_PATTERN_PREFIX;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.GenericApplicationContext;

import skaro.pokedex.sdk.messaging.MessageReceiverHotStream;
import skaro.pokedex.sdk.messaging.dispatch.DispatchTopicMessagingConfiguration;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequestReceiver;
import skaro.pokedex.sdk.worker.command.registration.CommandRegistrar;
import skaro.pokedex.sdk.worker.command.registration.CommandRegistration;

@Configuration
@Import(DispatchTopicMessagingConfiguration.class)
public class WorkerMessageListenConfiguration {
	public static final String COMMAND_DISPATCH_QUEUE_BEAN = "dispatchQueueBean";
	public static final String COMMAND_BINDINGS_BEAN = "commandBindings";
	public static final String COMMAND_LISTENER_CONTAINER_BEAN = "commandListenerContainer";
	public static final String COMMAND_MESSAGE_LISTENER_ADAPTER_BEAN = "commandMessageListenerAdapter";
	
	@Bean
	public MessageReceiverHotStream<WorkRequest> messageReceiver() {
		return new WorkRequestReceiver();
	}
	
	@Bean(COMMAND_MESSAGE_LISTENER_ADAPTER_BEAN)
	public MessageListenerAdapter listenerAdapter(MessageReceiverHotStream<WorkRequest> receiver) {
		return new MessageListenerAdapter(receiver, MessageReceiverHotStream.RECEIVE_METHOD_NAME);
	}
	
	@Bean(COMMAND_DISPATCH_QUEUE_BEAN)
	public Queue queue() {
		return new AnonymousQueue();
	}
	
	@Bean(COMMAND_BINDINGS_BEAN)
	public List<Binding> bindings(GenericApplicationContext context, 
			@Qualifier(COMMAND_DISPATCH_QUEUE_BEAN) Queue queue, 
			@Qualifier(DISPATCH_TOPIC_EXCHANGE_BEAN) TopicExchange topic,
			CommandRegistrar commandRegistrar) {
		
		return commandRegistrar.getCommandRegistrations().stream()
				.flatMap(commandRegistration -> createBindings(commandRegistration, queue, topic))
				.map(binding -> registerBinding(context, binding))
				.collect(Collectors.toList());
	}
	
	@Bean(COMMAND_LISTENER_CONTAINER_BEAN)
	public MessageListenerContainer messageListenerContainer(ConnectionFactory connectionFactory, 
			@Qualifier(COMMAND_DISPATCH_QUEUE_BEAN) Queue queue, 
			@Qualifier(COMMAND_MESSAGE_LISTENER_ADAPTER_BEAN) MessageListenerAdapter adapter,
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
