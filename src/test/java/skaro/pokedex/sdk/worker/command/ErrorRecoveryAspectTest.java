package skaro.pokedex.sdk.worker.command;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
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
import discord4j.rest.request.DiscordWebResponse;
import discord4j.rest.request.Router;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import skaro.pokedex.sdk.client.Language;
import skaro.pokedex.sdk.messaging.dispatch.AnsweredWorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkStatus;
import skaro.pokedex.sdk.worker.command.specification.DiscordEmbedField;
import skaro.pokedex.sdk.worker.command.specification.DiscordEmbedLocaleSpec;
import skaro.pokedex.sdk.worker.command.specification.DiscordEmbedSpec;

@ExtendWith(SpringExtension.class)
public class ErrorRecoveryAspectTest {

	@Mock
	private Router router;
	
	private ErrorRecoveryAspectConfiguration aspect;
	
	@BeforeEach
	public void setup() throws URISyntaxException {
		DiscordEmbedLocaleSpec localeSpec = setupLocaleSpec();
		aspect = new ErrorRecoveryAspectConfiguration(router, localeSpec);
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testHandleCommandErrorAdvice_joinPointThrowsError() throws Throwable {
		WorkRequest workRequest = new WorkRequest();
		workRequest.setChannelId(UUID.randomUUID().toString());
		workRequest.setLanguage(Language.ENGLISH);
		DiscordWebResponse discordResponse = Mockito.mock(DiscordWebResponse.class);
		
		ProceedingJoinPoint joinPoint = Mockito.mock(ProceedingJoinPoint.class);
		Object[] joinPointArguments = new Object[] {workRequest};
		
		Mockito.when(joinPoint.getArgs())
			.thenReturn(joinPointArguments);
		Mockito.when(joinPoint.proceed(joinPointArguments))
			.thenReturn(Mono.error(new Throwable("reason")));
		Mockito.when(router.exchange(ArgumentMatchers.any()))
			.thenReturn(discordResponse);
		Mockito.when(discordResponse.mono())
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
		workRequest.setChannelId(UUID.randomUUID().toString());
		workRequest.setLanguage(Language.ENGLISH);
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
	
	
	
	private DiscordEmbedLocaleSpec setupLocaleSpec() throws URISyntaxException {
		DiscordEmbedLocaleSpec spec = new DiscordEmbedLocaleSpec();
		spec.setColor(1);
		spec.setThumbnail(new URI("http://localhost"));
		
		DiscordEmbedSpec embedSpec = new DiscordEmbedSpec();
		embedSpec.setTitle("title");
		embedSpec.setDescription("description");
		DiscordEmbedField embedField = new DiscordEmbedField();
		embedField.setName("Foo");
		embedField.setValue("Bar");
		embedSpec.setFields(List.of(embedField, embedField, embedField));
		
		Map<Language, DiscordEmbedSpec> embedSpecs = Map.of(Language.ENGLISH, embedSpec);
		spec.setEmbedSpecs(embedSpecs);
		
		return spec;
	}
	
	
}
