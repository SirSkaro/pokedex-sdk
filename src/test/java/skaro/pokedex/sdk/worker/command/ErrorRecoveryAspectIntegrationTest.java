package skaro.pokedex.sdk.worker.command;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import discord4j.rest.http.client.ClientResponse;
import discord4j.rest.request.DiscordWebResponse;
import discord4j.rest.request.Router;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import skaro.pokedex.sdk.TestApplication;
import skaro.pokedex.sdk.client.Language;
import skaro.pokedex.sdk.messaging.dispatch.AnsweredWorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkStatus;
import skaro.pokedex.sdk.worker.command.ErrorRecoveryAspectIntegrationTest.ErrorRecoveryAspectTestConfiguration;
import skaro.pokedex.sdk.worker.command.specification.DiscordEmbedField;
import skaro.pokedex.sdk.worker.command.specification.DiscordEmbedLocaleSpec;
import skaro.pokedex.sdk.worker.command.specification.DiscordEmbedSpec;
import skaro.pokedex.sdk.worker.command.validation.ArgumentValidationChainAspectConfiguration;
import skaro.pokedex.sdk.worker.command.validation.Filter;
import skaro.pokedex.sdk.worker.command.validation.ValidationFilter;
import skaro.pokedex.sdk.worker.command.validation.ValidationFilterChain;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TestApplication.class)
@Import(ErrorRecoveryAspectTestConfiguration.class)
public class ErrorRecoveryAspectIntegrationTest {
	private static final String EXACTLY_ONE_ARGUMENT_VALIDATION_BEAN = "exactlyOne";
	
	private static AnsweredWorkRequest validatorAnswer = new AnsweredWorkRequest();
	private static AnsweredWorkRequest commandAnswer = new AnsweredWorkRequest();
	private static Consumer<AnsweredWorkRequest> assertErrorAnswer = answer -> {
		assertEquals(WorkStatus.ERROR, answer.getStatus());
	};
	
	@Autowired
	private AuthorModifierCommand command;
	@Autowired
	private Router router;
	private WorkRequest request;
	
	@BeforeEach
	public void setup() {
		request = new WorkRequest();
		request.setChannelId(UUID.randomUUID().toString());
		request.setLanguage(Language.ENGLISH);
		
		DiscordWebResponse discordResponse = Mockito.mock(DiscordWebResponse.class);
		Mockito.when(router.exchange(ArgumentMatchers.any()))
			.thenReturn(discordResponse);
		Mockito.when(discordResponse.mono())
			.thenReturn(Mono.just(Mockito.mock(ClientResponse.class)));
	}
	
	
	@Test
	public void noViolationsOrErrors() {
		request.setArguments(List.of("just one argument"));
		request.setAuthorId("FooBar");
		
		StepVerifier.create(command.execute(request))
			.expectNext(commandAnswer)
			.expectComplete()
			.verify();
	}
	
	@Test
	public void validatorError_executedFromCommand() {
		request.setArguments(null);
		
		StepVerifier.create(command.execute(request))
		.assertNext(assertErrorAnswer)
		.expectComplete()
		.verify();
	}
	
	@Test
	public void commandError() {
		request.setArguments(List.of("one argument"));
		
		StepVerifier.create(command.execute(request))
			.assertNext(assertErrorAnswer)
			.expectComplete()
			.verify();
	}
	
	@TestConfiguration
	@Import({
		ErrorRecoveryAspectConfiguration.class,
		ArgumentValidationChainAspectConfiguration.class
	})
	static class ErrorRecoveryAspectTestConfiguration {
		
		@MockBean
		Router router;
		
		@Bean(DefaultWorkerCommandConfiguration.ERROR_LOCALE_SPEC_BEAN)
		DiscordEmbedLocaleSpec localeSpec() throws URISyntaxException {
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
		
		@Bean(EXACTLY_ONE_ARGUMENT_VALIDATION_BEAN)
		ValidationFilter hasOneOrMoreArgumentsValidationFilter() {
			return workRequest -> workRequest.getArguments().isEmpty()
					? Mono.just(validatorAnswer)
					: Mono.empty();
		}
		
		@Bean
		AuthorModifierCommand command() {
			return new AuthorModifierCommand();
		}
		
	}
	
	@ValidationFilterChain({
		@Filter(value = ValidationFilter.class, beanName = EXACTLY_ONE_ARGUMENT_VALIDATION_BEAN)
	})
	static class AuthorModifierCommand implements Command {

		@Override
		public Mono<AnsweredWorkRequest> execute(WorkRequest request) {
			request.getAuthorId().replace("Foo", request.getArguments().get(0));
			return Mono.just(commandAnswer);
		}
		
	}
	
}
