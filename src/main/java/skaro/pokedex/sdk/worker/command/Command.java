package skaro.pokedex.sdk.worker.command;

import reactor.core.publisher.Mono;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequest;

public interface Command {
	
	Mono<WorkRequest> execute(WorkRequest request);
	
}
