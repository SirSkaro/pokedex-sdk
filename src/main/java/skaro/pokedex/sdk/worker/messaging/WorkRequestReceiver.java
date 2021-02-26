package skaro.pokedex.sdk.worker.messaging;

import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.FluxSink.OverflowStrategy;
import reactor.core.scheduler.Scheduler;
import skaro.pokedex.sdk.messaging.MessageReceiver;

public class WorkRequestReceiver implements MessageReceiver<WorkRequest> {

	private Flux<WorkRequest> publish;
	private FluxSink<WorkRequest> fluxSink;
	
	public WorkRequestReceiver() {
		publish = Flux.create(sink -> {this.fluxSink = sink;}, OverflowStrategy.BUFFER);
	}

	@Override
	public void receive(WorkRequest message) {
		fluxSink.next(message);
	}
	
	@Override
	public Flux<WorkRequest> streamMessages(Scheduler scheduler) {
		return publish.publishOn(scheduler);
	}

}
