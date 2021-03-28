package skaro.pokedex.sdk.worker.command.manager;

import java.util.Calendar;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.util.function.Tuple2;
import skaro.pokedex.sdk.messaging.dispatch.AnsweredWorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequest;
import skaro.pokedex.sdk.worker.command.registration.CommandRegistrar;

public class ReportingCommandManager implements CommandManager {

	private CommandRegistrar commandRegistrar;
	private Scheduler scheduler;
	
	public ReportingCommandManager(CommandRegistrar commandRegistrar, Scheduler scheduler) {
		this.commandRegistrar = commandRegistrar;
		this.scheduler = scheduler;
	}

	@Override
	public Mono<AnsweredWorkRequest> forward(WorkRequest request) {
		return Mono.justOrEmpty(commandRegistrar.getCommandByNameOrAlias(request.getCommmand()))
			.flatMap(command -> command.execute(request))
			.elapsed(scheduler)
			.map(this::toAnsweredRequest);
	}

	private AnsweredWorkRequest toAnsweredRequest(Tuple2<Long, WorkRequest> requestWithProcessTime) {
		WorkRequest request = requestWithProcessTime.getT2();
		Long processTime = requestWithProcessTime.getT1();
		
		AnsweredWorkRequest fulfilledRequest = new AnsweredWorkRequest();
		fulfilledRequest.setWorkRequest(request);
		fulfilledRequest.setProcessDate(Calendar.getInstance());
		fulfilledRequest.setProcessTime(processTime);
		
		return fulfilledRequest;
	}
	
}
