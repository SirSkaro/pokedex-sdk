package skaro.pokedex.sdk.worker.command.error;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URISyntaxException;
import java.util.UUID;
import java.util.function.Consumer;

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
import skaro.pokedex.sdk.client.Language;
import skaro.pokedex.sdk.discord.DiscordMessageDirector;
import skaro.pokedex.sdk.messaging.dispatch.AnsweredWorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkStatus;

@ExtendWith(SpringExtension.class)
public class ErrorRecoveryAspectTest {

	@Mock
	private DiscordMessageDirector<ErrorMessageContent> messageDirector;
	private ErrorRecoveryAspectConfiguration aspect;
	
	@BeforeEach
	public void setup() throws URISyntaxException {
		aspect = new ErrorRecoveryAspectConfiguration(messageDirector);
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testHandleCommandErrorAdvice_joinPointThrowsError() throws Throwable {
		WorkRequest workRequest = new WorkRequest();
		workRequest.setChannelId(UUID.randomUUID().toString());
		workRequest.setLanguage(Language.ENGLISH);
		
		ProceedingJoinPoint joinPoint = Mockito.mock(ProceedingJoinPoint.class);
		Object[] joinPointArguments = new Object[] {workRequest};
		
		Mockito.when(joinPoint.getArgs())
			.thenReturn(joinPointArguments);
		Mockito.when(joinPoint.proceed(joinPointArguments))
			.thenReturn(Mono.error(new Throwable("reason")));
		Mockito.when(messageDirector.createDiscordMessage(ArgumentMatchers.any(), ArgumentMatchers.eq(workRequest.getChannelId())))
			.thenReturn(Mono.just(Mockito.mock(ClientResponse.class)));
		
		Mono<AnsweredWorkRequest> result = (Mono<AnsweredWorkRequest>) aspect.handleCommandErrorAdvice(joinPoint, workRequest);
		Consumer<AnsweredWorkRequest> assertErrorAnswer = answer -> {
			assertEquals(WorkStatus.ERROR, answer.getStatus());
			assertEquals(workRequest, answer.getWorkRequest());
		};
		
		StepVerifier.create(result)
			.assertNext(assertErrorAnswer)
			.expectComplete()
			.verify();
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testHandleCommandErrorAdvice_joinPointDoesNotThrowError() throws Throwable {
		WorkRequest workRequest = new WorkRequest();
		AnsweredWorkRequest answer = new AnsweredWorkRequest();
		
		ProceedingJoinPoint joinPoint = Mockito.mock(ProceedingJoinPoint.class);
		Object[] joinPointArguments = new Object[] {workRequest};
		
		Mockito.when(joinPoint.getArgs())
			.thenReturn(joinPointArguments);
		Mockito.when(joinPoint.proceed(joinPointArguments))
			.thenReturn(Mono.just(answer));
		
		Mono<AnsweredWorkRequest> result = (Mono<AnsweredWorkRequest>) aspect.handleCommandErrorAdvice(joinPoint, workRequest);
		
		StepVerifier.create(result)
			.expectNext(answer)
			.expectComplete()
			.verify();
	}
	
}
