package skaro.pokedex.sdk.worker.command.ratelimit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static skaro.pokedex.sdk.worker.command.ratelimit.BaseRateLimitConfiguration.COMMAND_BUCKET_POOL_BEAN;
import static skaro.pokedex.sdk.worker.command.ratelimit.BaseRateLimitConfiguration.WARNING_MESSAGE_BUCKET_POOL_BEAN;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import discord4j.rest.http.client.ClientResponse;
import io.github.bucket4j.AsyncBucket;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.ConsumptionProbe;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import skaro.pokedex.sdk.TestApplication;
import skaro.pokedex.sdk.client.Language;
import skaro.pokedex.sdk.discord.DiscordRouterFacade;
import skaro.pokedex.sdk.messaging.dispatch.AnsweredWorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkStatus;
import skaro.pokedex.sdk.worker.command.Command;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TestApplication.class)
@Import(RateLimitAspectIntegrationTest.RateLimitAspectIntegrationTestConfiguration.class)
@PropertySource("classpath:sdk.properties")
public class RateLimitAspectIntegrationTest {
	private static final String NOT_RATE_LIMTED_BUCKET_BEAN = "notRateLimitedBucket";
	private static final String RATE_LIMTED_BUCKET_BEAN = "rateLimitedBucket";
	private static AnsweredWorkRequest successfulAnswer = new AnsweredWorkRequest();
	
	@Autowired
	private DiscordRouterFacade router;
	@Autowired
	@Qualifier(COMMAND_BUCKET_POOL_BEAN)
	private BucketPool commandBucketPool;
	@Autowired
	@Qualifier(WARNING_MESSAGE_BUCKET_POOL_BEAN)
	private BucketPool messageBucketPool;
	@Autowired
	@Qualifier(NOT_RATE_LIMTED_BUCKET_BEAN)
	private AsyncBucket notRatelimitedBucket;
	@Autowired
	@Qualifier(RATE_LIMTED_BUCKET_BEAN)
	private AsyncBucket ratelimitedBucket;
	
	@Autowired
	private RateLimitedCommand command;
	private WorkRequest request;
	
	@BeforeEach
	public void setup() {
		request = new WorkRequest();
		request.setGuildId(UUID.randomUUID().toString());
		request.setLanguage(Language.ENGLISH);
		
		Mockito.when(router.createMessage(any(), any()))
			.thenReturn(Mono.just(Mockito.mock(ClientResponse.class)));
	}
	
	private void notOnCooldownSetup() {
		Mockito.when(commandBucketPool.getBucket(anyString(), any()))
			.thenReturn(Mono.just(notRatelimitedBucket));
	}
	
	private void onCooldownSetup() {
		Mockito.when(commandBucketPool.getBucket(anyString(), any()))
			.thenReturn(Mono.just(ratelimitedBucket));
		Mockito.when(messageBucketPool.getBucket(anyString(), any()))
			.thenReturn(Mono.just(notRatelimitedBucket));
	}
	
	private void commandAndWarningMessageCooldownSetup() {
		Mockito.when(commandBucketPool.getBucket(anyString(), any()))
			.thenReturn(Mono.just(ratelimitedBucket));
		Mockito.when(messageBucketPool.getBucket(anyString(), any()))
			.thenReturn(Mono.just(ratelimitedBucket));
	}
	
	@Test
	public void commandNotOnCooldown() {
		notOnCooldownSetup();
		
		StepVerifier.create(command.execute(request))
			.expectNext(successfulAnswer)
			.expectComplete()
			.verify();
	}
	
	@Test
	public void commandOnCooldown() {
		onCooldownSetup();
		
		StepVerifier.create(command.execute(request))
			.assertNext(answer -> assertEquals(WorkStatus.RATE_LIMIT, answer.getStatus()))
			.expectComplete()
			.verify();
	}
	
	@Test
	public void warningMessageOnCooldown() {
		commandAndWarningMessageCooldownSetup();
		
		StepVerifier.create(command.execute(request))
			.assertNext(answer -> assertEquals(WorkStatus.RATE_LIMIT, answer.getStatus()))
			.expectComplete()
			.verify();
	}
	
	@TestConfiguration
	@Import({
		RateLimitAspectConfiguration.class,
		BaseRateLimitConfiguration.class
	})
	static class RateLimitAspectIntegrationTestConfiguration {
		@MockBean(name = COMMAND_BUCKET_POOL_BEAN)
		BucketPool commandBucketPool;
		@MockBean(name = WARNING_MESSAGE_BUCKET_POOL_BEAN)
		BucketPool messageBucketPool;
		@MockBean
		Bandwidth messageBandwidth;
		@MockBean
		DiscordRouterFacade rateLimitFacade;
		
		@Bean(NOT_RATE_LIMTED_BUCKET_BEAN)
		AsyncBucket notRatelimitedBucket() {
			AsyncBucket bucket = Mockito.mock(AsyncBucket.class);
			ConsumptionProbe probe = Mockito.mock(ConsumptionProbe.class);
			Mockito.when(bucket.tryConsumeAndReturnRemaining(ArgumentMatchers.anyLong()))
				.thenReturn(CompletableFuture.completedFuture(probe));
			Mockito.when(probe.isConsumed())
				.thenReturn(true);
			
			return bucket;
		}
		
		@Bean(RATE_LIMTED_BUCKET_BEAN)
		AsyncBucket ratelimitedBucket() {
			AsyncBucket bucket = Mockito.mock(AsyncBucket.class);
			ConsumptionProbe probe = Mockito.mock(ConsumptionProbe.class);
			Mockito.when(bucket.tryConsumeAndReturnRemaining(ArgumentMatchers.anyLong()))
				.thenReturn(CompletableFuture.completedFuture(probe));
			Mockito.when(probe.isConsumed())
				.thenReturn(false);
			
			return bucket;
		}
		
		@Bean
		RateLimitedCommand command() {
			return new RateLimitedCommand();
		}
		
	}
	
	@RateLimit(command = RateLimitedCommand.class, seconds = 1, requests = 1)
	static class RateLimitedCommand implements Command {

		@Override
		public Mono<AnsweredWorkRequest> execute(WorkRequest request) {
			return Mono.just(successfulAnswer);
		}
		
	}
	
}
