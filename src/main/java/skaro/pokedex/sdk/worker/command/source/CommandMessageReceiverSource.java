package skaro.pokedex.sdk.worker.command.source;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import skaro.pokedex.sdk.messaging.MessageReceiverHotStream;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequestReport;
import skaro.pokedex.sdk.worker.command.manager.CommandManager;

public class CommandMessageReceiverSource implements CommandSource {
	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private CommandManager manager;
	private MessageReceiverHotStream<WorkRequest> receiver;
	private Scheduler scheduler;
	
	public CommandMessageReceiverSource(CommandManager manager, MessageReceiverHotStream<WorkRequest> receiver, Scheduler scheduler) {
		this.manager = manager; 
		this.receiver = receiver;
		this.scheduler = scheduler;
	}

	@Override
	public Flux<WorkRequestReport> stream() {
		LOG.info("Listening for commands");
		
		return receiver.streamMessages(scheduler)
			.flatMap(this::forwardMessage);
	}
	
	private Mono<WorkRequestReport> forwardMessage(WorkRequest workRequest) {
		return manager.forward(workRequest)
				.onErrorResume(this::handleError);
	}
	
	private Mono<WorkRequestReport> handleError(Throwable error) {
		LOG.error("Error in consuming command", error);
		return Mono.empty();
	}

}
