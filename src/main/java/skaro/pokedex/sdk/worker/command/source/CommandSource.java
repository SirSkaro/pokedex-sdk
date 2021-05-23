package skaro.pokedex.sdk.worker.command.source;

import reactor.core.publisher.Flux;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequestReport;

public interface CommandSource {

	Flux<WorkRequestReport> stream();
	
}
