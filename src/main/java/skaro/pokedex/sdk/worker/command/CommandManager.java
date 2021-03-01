package skaro.pokedex.sdk.worker.command;

import reactor.core.publisher.Mono;
import skaro.pokedex.sdk.worker.messaging.AnsweredWorkRequest;
import skaro.pokedex.sdk.worker.messaging.WorkRequest;

public interface CommandManager {

	Mono<AnsweredWorkRequest> forward(WorkRequest request);
	
}
