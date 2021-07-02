package skaro.pokedex.sdk.worker.command.ratelimit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.github.bucket4j.AsyncBucket;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.ConsumptionProbe;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.StepVerifier.FirstStep;
import skaro.pokedex.sdk.worker.command.Command;

@ExtendWith(SpringExtension.class)
public class DualPoolRateLimitFacadeTest {
	
	@Mock
	private BucketPool commandBucketPool;
	@Mock
	private BucketPool warningMessageBucketPool;
	@Mock
	private Bandwidth warningMessageBandwidth;
	
	private DualPoolRateLimitFacade facade;
	
	@BeforeEach
	public void setup() {
		facade = new DualPoolRateLimitFacade(commandBucketPool, warningMessageBucketPool, warningMessageBandwidth);
	}
	
	@Test
	public void isCommandOnCooldownTest_onCooldown() {
		whenCommandIsOnCooldown(true);
		thenFacadeReportsCommandCooldown(true);
	}
	
	@Test
	public void isCommandOnCooldownTest_notOnCooldown() {
		whenCommandIsOnCooldown(false);
		thenFacadeReportsCommandCooldown(false);
	}
	
	@Test
	public void isWarningMessageOnCooldownTest_onCooldown() {
		whenWarningMessageIsOnCooldown(true);
		thenFacadeReportsWarningMessageCooldown(true);
	}
	
	@Test
	public void isWarningMessageOnCooldownTest_notOnCooldown() {
		whenWarningMessageIsOnCooldown(false);
		thenFacadeReportsWarningMessageCooldown(false);
	}
	
	private void whenCommandIsOnCooldown(boolean isOnCooldown) {
		AsyncBucket bucket = Mockito.mock(AsyncBucket.class);
		Mockito.when(commandBucketPool.getBucket(anyString(), any(Bandwidth.class)))
			.thenReturn(Mono.just(bucket));
		
		boolean wasTokenConsumed = !isOnCooldown;
		whenBucketProbeReturns(bucket, wasTokenConsumed);
	}
	
	private void whenWarningMessageIsOnCooldown(boolean isOnCooldown) {
		AsyncBucket bucket = Mockito.mock(AsyncBucket.class);
		Mockito.when(warningMessageBucketPool.getBucket(anyString(), eq(warningMessageBandwidth)))
			.thenReturn(Mono.just(bucket));
		
		boolean wasTokenConsumed = !isOnCooldown;
		whenBucketProbeReturns(bucket, wasTokenConsumed);
	}
	
	private void thenFacadeReportsCommandCooldown(boolean isOnCooldown) {
		RateLimit rateLimit = createRateLimit();
		String guildId = UUID.randomUUID().toString();
		
		FirstStep<CooldownReport> report = StepVerifier.create(facade.isCommandOnCooldownForGuild(guildId, rateLimit));
		assertExpectedCooldownReport(report, isOnCooldown);	
	}
	
	private void thenFacadeReportsWarningMessageCooldown(boolean isOnCooldown) {
		String guildId = UUID.randomUUID().toString();
		
		FirstStep<CooldownReport> report = StepVerifier.create(facade.isWarningMessageOnCooldownForGuild(guildId, Command.class));
		assertExpectedCooldownReport(report, isOnCooldown);	
	}

	private void whenBucketProbeReturns(AsyncBucket bucket, boolean tokenConsumed) {
		ConsumptionProbe probe = Mockito.mock(ConsumptionProbe.class);
		
		Mockito.when(bucket.tryConsumeAndReturnRemaining(ArgumentMatchers.anyLong()))
			.thenReturn(CompletableFuture.completedFuture(probe));
		Mockito.when(probe.isConsumed())
			.thenReturn(tokenConsumed);
	}
	
	private RateLimit createRateLimit() {
		RateLimit rateLimit = Mockito.mock(RateLimit.class);
		Mockito.when(rateLimit.requests()).thenReturn(1);
		Mockito.when(rateLimit.seconds()).thenReturn(1);
		Mockito.when(rateLimit.command()).thenAnswer(answer -> Command.class);
		
		return rateLimit;
	}
	
	private void assertExpectedCooldownReport(FirstStep<CooldownReport> report, boolean isOnCooldown) {
		report.assertNext(cooldownReport -> assertEquals(isOnCooldown, cooldownReport.isOnCooldown()))
			.expectComplete()
			.verify();
	}
	
}
