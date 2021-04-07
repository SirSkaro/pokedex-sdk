package skaro.pokedex.sdk.worker.command;

import reactor.core.publisher.Mono;
import skaro.pokedex.sdk.messaging.dispatch.AnsweredWorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequest;

@FunctionalInterface
public interface Command {
	
	Mono<AnsweredWorkRequest> execute(WorkRequest request);
	
}
