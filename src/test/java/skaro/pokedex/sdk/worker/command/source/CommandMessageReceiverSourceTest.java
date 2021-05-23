package skaro.pokedex.sdk.worker.command.source;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.test.StepVerifier;
import skaro.pokedex.sdk.messaging.MessageReceiver;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequestReport;
import skaro.pokedex.sdk.worker.command.manager.CommandManager;

@ExtendWith(SpringExtension.class)
public class CommandMessageReceiverSourceTest {

	@Mock
	private CommandManager manager;
	@Mock
	private MessageReceiver<WorkRequest> receiver;
	@Mock
	private Scheduler scheduler;
	
	private CommandMessageReceiverSource source;
	
	@BeforeEach
	public void setup() {
		source = new CommandMessageReceiverSource(manager, receiver, scheduler);
	}
	
	@Test
	public void testRun() {
		WorkRequest workRequest = new WorkRequest();
		WorkRequestReport report = new WorkRequestReport();
		
		Mockito.when(receiver.streamMessages(scheduler))
			.thenReturn(Flux.just(workRequest));
		Mockito.when(manager.forward(workRequest))
			.thenReturn(Mono.just(report));
		
		StepVerifier.create(source.stream())
			.expectNext(report)
			.expectComplete()
			.verify();
	}
	
	@Test
	public void testRun_managerThrowsError() {
		WorkRequest workRequest1 = new WorkRequest();
		WorkRequest workRequest2 = new WorkRequest();
		WorkRequestReport report = new WorkRequestReport();
		
		Mockito.when(receiver.streamMessages(scheduler))
			.thenReturn(Flux.just(workRequest1, workRequest2));
		Mockito.when(manager.forward(workRequest1))
			.thenReturn(Mono.error(new Throwable()));
		Mockito.when(manager.forward(workRequest2))
			.thenReturn(Mono.just(report));
		
		StepVerifier.create(source.stream())
			.expectNext(report)
			.expectComplete()
			.verify();
	}
	
}
