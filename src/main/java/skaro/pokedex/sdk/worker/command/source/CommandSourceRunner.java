package skaro.pokedex.sdk.worker.command.source;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import skaro.pokedex.sdk.messaging.MessageReceiver;
import skaro.pokedex.sdk.messaging.dispatch.AnsweredWorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequest;
import skaro.pokedex.sdk.worker.command.manager.CommandManager;

public class CommandSourceRunner implements CommandSource, CommandLineRunner {

	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private CommandManager manager;
	private MessageReceiver<WorkRequest> receiver;
	private Scheduler scheduler;
	
	public CommandSourceRunner(CommandManager manager, MessageReceiver<WorkRequest> receiver, Scheduler scheduler) {
		this.manager = manager; 
		this.receiver = receiver;
		this.scheduler = scheduler;
	}

	@Override
	public void run(String... args) throws Exception {
		LOG.info("Listening for commands");
		
		receiver.streamMessages(scheduler)
			.flatMap(manager::forward)
			.onErrorResume(this::handleError)
			.subscribe();
	}
	
	private Mono<AnsweredWorkRequest> handleError(Throwable error) {
		LOG.error("Error in consuming command", error);
		return Mono.empty();
	}

}
