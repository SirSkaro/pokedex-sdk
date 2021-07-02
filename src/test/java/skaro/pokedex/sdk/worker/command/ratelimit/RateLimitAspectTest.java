package skaro.pokedex.sdk.worker.command.ratelimit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import java.util.UUID;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import discord4j.rest.http.client.ClientResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import skaro.pokedex.sdk.discord.DiscordMessageDirector;
import skaro.pokedex.sdk.messaging.dispatch.AnsweredWorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkStatus;
import skaro.pokedex.sdk.worker.command.Command;

@ExtendWith(SpringExtension.class)
public class RateLimitAspectTest {

	@Mock
	private RateLimitFacade rateLimitFacade;
	@Mock
	private DiscordMessageDirector<RateLimitMessageContent> messageDirector;
	@Mock
	ProceedingJoinPoint joinPoint;
	
	private RateLimitAspectConfiguration aspect;
	
	@BeforeEach
	public void setup() {
		aspect = new RateLimitAspectConfiguration(rateLimitFacade, messageDirector);
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void limitTest_commandIsNotOnCooldown() throws Throwable {
		AnsweredWorkRequest answer = new AnsweredWorkRequest();
		RateLimit rateLimit = Mockito.mock(RateLimit.class);
		CooldownReport report = new CooldownReport();
		report.setOnCooldown(false);
		WorkRequest workRequest = new WorkRequest();
		workRequest.setGuildId(UUID.randomUUID().toString());
		
		Mockito.when(joinPoint.proceed(ArgumentMatchers.any()))
			.thenReturn(Mono.just(answer));
		Mockito.when(rateLimitFacade.isCommandOnCooldownForGuild(workRequest.getGuildId(), rateLimit))
			.thenReturn(Mono.just(report));
		
		StepVerifier.create((Mono<AnsweredWorkRequest>)aspect.limit(joinPoint, rateLimit, workRequest))
			.expectNext(answer)
			.expectComplete()
			.verify();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void limitTest_commandIsOnCooldown() {
		RateLimit rateLimit = Mockito.mock(RateLimit.class);
		CooldownReport commandCooldownreport = new CooldownReport();
		commandCooldownreport.setOnCooldown(true);
		CooldownReport messageCooldownReport = new CooldownReport();
		messageCooldownReport.setOnCooldown(false);
		WorkRequest workRequest = new WorkRequest();
		workRequest.setGuildId(UUID.randomUUID().toString());
		workRequest.setChannelId(UUID.randomUUID().toString());
		
		Mockito.when(rateLimit.command()).thenAnswer(stubbing -> Command.class);
		Mockito.when(rateLimitFacade.isCommandOnCooldownForGuild(workRequest.getGuildId(), rateLimit))
			.thenReturn(Mono.just(commandCooldownreport));
		Mockito.when(rateLimitFacade.isWarningMessageOnCooldownForGuild(workRequest.getGuildId(), rateLimit.command()))
			.thenReturn(Mono.just(messageCooldownReport));
		Mockito.when(messageDirector.createDiscordMessage(any(), eq(workRequest.getChannelId())))
			.thenReturn(Mono.just(Mockito.mock(ClientResponse.class)));
		
		StepVerifier.create((Mono<AnsweredWorkRequest>)aspect.limit(joinPoint, rateLimit, workRequest))
			.assertNext(answer -> assertEquals(WorkStatus.RATE_LIMIT, answer.getStatus()))
			.expectComplete()
			.verify();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void limitTest_commandIsOnCooldown_warningMessageIsOnCooldown() {
		RateLimit rateLimit = Mockito.mock(RateLimit.class);
		CooldownReport commandCooldownreport = new CooldownReport();
		commandCooldownreport.setOnCooldown(true);
		CooldownReport messageCooldownReport = new CooldownReport();
		messageCooldownReport.setOnCooldown(true);
		WorkRequest workRequest = new WorkRequest();
		workRequest.setGuildId(UUID.randomUUID().toString());
		
		Mockito.when(rateLimit.command()).thenAnswer(stubbing -> Command.class);
		Mockito.when(rateLimitFacade.isCommandOnCooldownForGuild(workRequest.getGuildId(), rateLimit))
			.thenReturn(Mono.just(commandCooldownreport));
		Mockito.when(rateLimitFacade.isWarningMessageOnCooldownForGuild(workRequest.getGuildId(), rateLimit.command()))
			.thenReturn(Mono.just(messageCooldownReport));
		
		StepVerifier.create((Mono<AnsweredWorkRequest>)aspect.limit(joinPoint, rateLimit, workRequest))
			.assertNext(answer -> assertEquals(WorkStatus.RATE_LIMIT, answer.getStatus()))
			.expectComplete()
			.verify();
		
		Mockito.verify(messageDirector, Mockito.never()).createDiscordMessage(any(), any());
	}
	
}
