package skaro.pokedex.sdk.messaging.dispatch;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
public class WorkRequestReceiverTest {

	private WorkRequestReceiver receiver;
	private Scheduler scheduler;
	
	@BeforeEach
	public void setup() {
		receiver = new WorkRequestReceiver();
		scheduler = Schedulers.parallel();
	}
	
	@Test
	public void steamMessagesTest() {
		WorkRequest workRequest1 = new WorkRequest();
		WorkRequest workRequest2 = new WorkRequest();
		
		scheduler.schedule(() -> receiver.receive(workRequest1), 100, TimeUnit.MILLISECONDS);
		scheduler.schedule(() -> receiver.receive(workRequest2), 150, TimeUnit.MILLISECONDS);
		
		StepVerifier.create(receiver.streamMessages(scheduler))
				.expectNext(workRequest1)
				.expectNext(workRequest2)
				.expectTimeout(Duration.of(200, TimeUnit.MILLISECONDS.toChronoUnit()))
				.verify();
	}
	
}
