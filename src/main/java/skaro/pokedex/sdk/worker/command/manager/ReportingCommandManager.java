package skaro.pokedex.sdk.worker.command.manager;

import java.util.Calendar;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.util.function.Tuple2;
import skaro.pokedex.sdk.messaging.dispatch.AnsweredWorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequestReport;
import skaro.pokedex.sdk.worker.command.registration.CommandRegistrar;

public class ReportingCommandManager implements CommandManager {

	private CommandRegistrar commandRegistrar;
	private Scheduler scheduler;
	
	public ReportingCommandManager(CommandRegistrar commandRegistrar, Scheduler scheduler) {
		this.commandRegistrar = commandRegistrar;
		this.scheduler = scheduler;
	}

	@Override
	public Mono<WorkRequestReport> forward(WorkRequest request) {
		return Mono.justOrEmpty(commandRegistrar.getCommandByNameOrAlias(request.getCommmand()))
			.flatMap(command -> command.execute(request))
			.elapsed(scheduler)
			.map(this::toAnsweredRequest);
	}

	private WorkRequestReport toAnsweredRequest(Tuple2<Long, AnsweredWorkRequest> requestWithProcessTime) {
		AnsweredWorkRequest request = requestWithProcessTime.getT2();
		Long processTime = requestWithProcessTime.getT1();
		
		WorkRequestReport report = new WorkRequestReport();
		report.setAnsweredWorkRequest(request);
		report.setProcessDate(Calendar.getInstance());
		report.setProcessTime(processTime);
		
		return report;
	}
	
}
