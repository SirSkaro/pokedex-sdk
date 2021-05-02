package skaro.pokedex.sdk.worker.command.validation;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import skaro.pokedex.sdk.TestApplication;
import skaro.pokedex.sdk.messaging.dispatch.AnsweredWorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequest;
import skaro.pokedex.sdk.worker.command.Command;
import skaro.pokedex.sdk.worker.command.validation.ArgumentValidationChainIntegrationTest.ArgumentValidationTestConfiguration;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TestApplication.class)
@Import(ArgumentValidationTestConfiguration.class)
public class ArgumentValidationChainIntegrationTest {
	private static final String ONE_OR_MORE_ARGUMENTS_VALIDATION_BEAN = "oneOrMore";
	private static final String EXACTLY_ONE_ARGUMENT_VALIDATION_BEAN = "exactlyOne";
	
	private static AnsweredWorkRequest oneOrMoreArgumentsInvalidAnswer = new AnsweredWorkRequest();
	private static AnsweredWorkRequest exactlyOnArgumentInvalidAnswer = new AnsweredWorkRequest();
	private static AnsweredWorkRequest successfulAnswer = new AnsweredWorkRequest();
	
	@Autowired
	private ValidatedCommand command;
	
	@Test
	public void noViolations() {
		WorkRequest request = new WorkRequest();
		request.setArguments(List.of("just one argument"));
		
		StepVerifier.create(command.execute(request))
			.expectNext(successfulAnswer)
			.expectComplete()
			.verify();
	}
	
	@Test
	public void violatesOneValidator() {
		WorkRequest request = new WorkRequest();
		request.setArguments(List.of("two", "arguments"));
		
		StepVerifier.create(command.execute(request))
			.expectNext(exactlyOnArgumentInvalidAnswer)
			.expectComplete()
			.verify();
	}
	
	@Test
	public void violatesBothValidator_orderMatters() {
		WorkRequest request = new WorkRequest();
		request.setArguments(List.of());
		
		StepVerifier.create(command.execute(request))
			.expectNext(oneOrMoreArgumentsInvalidAnswer)
			.expectComplete()
			.verify();
	}
	
	@TestConfiguration
	@Import(ArgumentValidationChainAspectConfiguration.class)
	static class ArgumentValidationTestConfiguration {
		
		@Bean(ONE_OR_MORE_ARGUMENTS_VALIDATION_BEAN)
		ValidationFilter hasOneOrMoreArgumentsValidationFilter() {
			return workRequest -> workRequest.getArguments().isEmpty()
					? Mono.just(oneOrMoreArgumentsInvalidAnswer)
					: Mono.empty();
		}
		
		@Bean(EXACTLY_ONE_ARGUMENT_VALIDATION_BEAN)
		ValidationFilter hasExactlyOneArgumentValidationFilter() {
			return workRequest -> workRequest.getArguments().size() == 1
					? Mono.empty()
					: Mono.just(exactlyOnArgumentInvalidAnswer);
		}
		
		@Bean
		ValidatedCommand command() {
			return new ValidatedCommand();
		}
		
	}
	
	@ValidationFilterChain({
		@Filter(value = ValidationFilter.class, beanName = ONE_OR_MORE_ARGUMENTS_VALIDATION_BEAN),
		@Filter(value = ValidationFilter.class, beanName = EXACTLY_ONE_ARGUMENT_VALIDATION_BEAN)
	})
	static class ValidatedCommand implements Command {

		@Override
		public Mono<AnsweredWorkRequest> execute(WorkRequest request) {
			return Mono.just(successfulAnswer);
		}
		
	}
	
}
