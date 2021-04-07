package skaro.pokedex.sdk.worker.command.validation;

import reactor.core.publisher.Mono;
import skaro.pokedex.sdk.messaging.dispatch.AnsweredWorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequest;

@FunctionalInterface
public interface ValidationFilter {

	Mono<AnsweredWorkRequest> filter(WorkRequest request);
	
}
