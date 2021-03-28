package skaro.pokedex.sdk.worker.command.manager;

import reactor.core.publisher.Mono;
import skaro.pokedex.sdk.messaging.dispatch.AnsweredWorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequest;

public interface CommandManager {

	Mono<AnsweredWorkRequest> forward(WorkRequest request);
	
}
