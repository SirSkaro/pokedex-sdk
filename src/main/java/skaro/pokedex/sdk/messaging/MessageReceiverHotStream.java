package skaro.pokedex.sdk.messaging;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;

public interface MessageReceiverHotStream<T> {
	public static final String RECEIVE_METHOD_NAME = "receive";
	
	void receive(T message);
	Flux<T> streamMessages(Scheduler scheduler);
	
}
