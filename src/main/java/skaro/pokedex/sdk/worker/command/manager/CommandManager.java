package skaro.pokedex.sdk.worker.command.manager;

import reactor.core.publisher.Mono;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequestReport;

public interface CommandManager {

	Mono<WorkRequestReport> forward(WorkRequest request);
	
}
