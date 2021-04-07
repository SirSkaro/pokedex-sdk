package skaro.pokedex.sdk.worker.command;

import java.util.Optional;
import java.util.function.Consumer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.test.StepVerifier;
import skaro.pokedex.sdk.messaging.dispatch.AnsweredWorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequestReport;
import skaro.pokedex.sdk.worker.command.manager.ReportingCommandManager;
import skaro.pokedex.sdk.worker.command.registration.CommandRegistrar;

@ExtendWith(SpringExtension.class)
public class ReportingCommandManagerTest {

	@Mock
	private CommandRegistrar commandRegistrar;
	@Mock
	private Scheduler scheduler;
	
	private ReportingCommandManager manager;
	
	@BeforeEach
	public void setup() {
		manager = new ReportingCommandManager(commandRegistrar, scheduler);
	}
	
	@Test
	public void testForwardRegisteredCommand() {
		Command command = Mockito.mock(Command.class);
		String requestedCommand = "bar";
		WorkRequest request = createWorkRequest(requestedCommand);
		AnsweredWorkRequest answeredRequest = createAnsweredWorkRequest(request);
		
		Mockito.when(commandRegistrar.getCommandByNameOrAlias(requestedCommand))
			.thenReturn(Optional.of(command));
		Mockito.when(command.execute(request))
			.thenAnswer(answer -> Mono.just(answeredRequest));
		
		Consumer<WorkRequestReport> assertReportContainsOriginalAnsweredWorkRequest = report -> {
			Assertions.assertEquals(answeredRequest, report.getAnsweredWorkRequest());
		};
		
		StepVerifier.create(manager.forward(request))
			.assertNext(assertReportContainsOriginalAnsweredWorkRequest)
			.expectComplete()
			.verify();
	}
	
	@Test
	public void testForwardUnregisteredCommand() {
		String requestedCommand = "foo";
		WorkRequest request = createWorkRequest(requestedCommand);
		Mockito.when(commandRegistrar.getCommandByNameOrAlias(requestedCommand))
			.thenReturn(Optional.empty());
		
		StepVerifier.create(manager.forward(request))
			.expectComplete()
			.verify();
	}
	
	private WorkRequest createWorkRequest(String commandName) {
		WorkRequest request = new WorkRequest();
		request.setCommmand(commandName);
		
		return request;
	}
	
	private AnsweredWorkRequest createAnsweredWorkRequest(WorkRequest request) {
		AnsweredWorkRequest answeredRequest = new AnsweredWorkRequest();
		answeredRequest.setWorkRequest(request);
		
		return answeredRequest;
	}
	
}
