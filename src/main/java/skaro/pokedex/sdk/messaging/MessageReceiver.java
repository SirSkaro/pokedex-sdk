package skaro.pokedex.sdk.messaging;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;

public interface MessageReceiver<T> {
	public static final String RECIEVE_METHOD_NAME = "receive";
	
	void receive(T message);
	Flux<T> streamMessages(Scheduler scheduler);
	
}
