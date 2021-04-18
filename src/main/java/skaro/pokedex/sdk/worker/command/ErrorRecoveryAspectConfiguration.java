package skaro.pokedex.sdk.worker.command;

import static skaro.pokedex.sdk.worker.command.DefaultWorkerCommandConfiguration.ERROR_LOCALE_SPEC_BEAN;
import static skaro.pokedex.sdk.worker.command.DefaultWorkerCommandConfiguration.ERROR_RECOVERY_ASPECT_ORDER;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.EmbedFieldData;
import discord4j.discordjson.json.EmbedThumbnailData;
import discord4j.discordjson.json.MessageCreateRequest;
import discord4j.rest.request.Router;
import discord4j.rest.route.Routes;
import reactor.core.publisher.Mono;
import skaro.pokedex.sdk.messaging.dispatch.AnsweredWorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkStatus;
import skaro.pokedex.sdk.worker.command.specification.DiscordEmbedLocaleSpec;
import skaro.pokedex.sdk.worker.command.specification.DiscordEmbedSpec;

@Aspect
@Configuration
@Order(ERROR_RECOVERY_ASPECT_ORDER)
public class ErrorRecoveryAspectConfiguration {
	private Router router;
	private DiscordEmbedLocaleSpec localeSpec;

	public ErrorRecoveryAspectConfiguration(Router router, @Qualifier(ERROR_LOCALE_SPEC_BEAN) DiscordEmbedLocaleSpec localeSpec) {
		this.router = router;
		this.localeSpec = localeSpec;
	}
	
	@Around("implementsCommand() && methodHasWorkRequestArgument(workRequest)")
	public Object handleCommandErrorAdvice(ProceedingJoinPoint joinPoint, WorkRequest workRequest) throws Throwable {
		return attemptProceed(joinPoint, workRequest);
	}
	
	@Around("implementsValidationFilter() && methodHasWorkRequestArgument(workRequest)")
	public Object handleValidatorErrorAdvice(ProceedingJoinPoint joinPoint, WorkRequest workRequest) throws Throwable {
		return attemptProceed(joinPoint, workRequest);
	}
	
	@Pointcut("args(workRequest,..)")
	private void methodHasWorkRequestArgument(WorkRequest workRequest) {}
	
	@Pointcut("target(skaro.pokedex.sdk.worker.command.Command)") 
	private void implementsCommand() {}
	
	@Pointcut("target(skaro.pokedex.sdk.worker.command.validation.ValidationFilter)")
	private void implementsValidationFilter() {}
	
	private Object attemptProceed(ProceedingJoinPoint joinPoint, WorkRequest workRequest) {
		try {
			return joinPoint.proceed(joinPoint.getArgs());
		} catch(Throwable e) {
			return sendErrorMessage(workRequest, e);
		}
	}
	
	private Mono<AnsweredWorkRequest> sendErrorMessage(WorkRequest workRequest, Throwable error) {
		AnsweredWorkRequest answer = new AnsweredWorkRequest();
		answer.setStatus(WorkStatus.ERROR);
		answer.setWorkRequest(workRequest);
		
		return Routes.MESSAGE_CREATE.newRequest(workRequest.getChannelId())
			.body(createErrorResponse(workRequest, error))
			.exchange(router)
			.mono()
			.thenReturn(answer);
	}
	
	private MessageCreateRequest createErrorResponse(WorkRequest workRequest, Throwable error) {
		DiscordEmbedSpec embedSpec = localeSpec.getEmbedSpecs().get(workRequest.getLanguage());
		
		EmbedFieldData technicalErrorField = EmbedFieldData.builder()
				.inline(true)
				.name(embedSpec.getFields().get(0).getName())
				.value(error.getMessage())
				.build();
		EmbedFieldData userInputField = EmbedFieldData.builder()
				.inline(true)
				.name(embedSpec.getFields().get(1).getName())
				.value(String.format("%s %s", workRequest.getCommmand(), workRequest.getArguments()))
				.build();
		EmbedFieldData supportServerLinkField = EmbedFieldData.builder()
				.inline(true)
				.name(embedSpec.getFields().get(2).getName())
				.value(embedSpec.getFields().get(2).getValue())
				.build();
		
		EmbedData embed = EmbedData.builder()
				.color(localeSpec.getColor())
				.title(embedSpec.getTitle())
				.description(embedSpec.getDescription())
				.addField(technicalErrorField)
				.addField(userInputField)
				.addField(supportServerLinkField)
				.thumbnail(EmbedThumbnailData.builder()
						.url(localeSpec.getThumbnail().toString())
						.build())
				.build();
		
		return MessageCreateRequest.builder()
				.embed(embed)
				.build();
	}
}
