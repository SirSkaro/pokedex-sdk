package skaro.pokedex.sdk.worker.command;

import static skaro.pokedex.sdk.worker.command.DefaultWorkerCommandConfiguration.ERROR_RECOVERY_ASPECT_ORDER;

import java.lang.invoke.MethodHandles;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.EmbedFieldData;
import discord4j.discordjson.json.MessageCreateRequest;
import discord4j.rest.request.Router;
import discord4j.rest.route.Routes;
import skaro.pokedex.sdk.messaging.dispatch.AnsweredWorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkStatus;

@Aspect
@Configuration
@Order(ERROR_RECOVERY_ASPECT_ORDER)
public class ErrorRecoveryAspectConfiguration {
	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private Router router;

	public ErrorRecoveryAspectConfiguration(Router router) {
		this.router = router;
	}
	
	@Around("implementsCommand() && methodHasWorkRequestArgument(workRequest)")
	public Object handleCommandErrorAdvice(ProceedingJoinPoint joinPoint, WorkRequest workRequest) throws Throwable {
		LOG.info("Error handling for command {}", workRequest.getCommmand());
		return attemptProceed(joinPoint, workRequest);
	}
	
	@Around("implementsValidationFilter() && methodHasWorkRequestArgument(workRequest)")
	public Object handleValidatorErrorAdvice(ProceedingJoinPoint joinPoint, WorkRequest workRequest) throws Throwable {
		LOG.info("Error handling for valdiator {}");
		return attemptProceed(joinPoint, workRequest);
	}
	
	@Pointcut("args(workRequest,..)")
	private void methodHasWorkRequestArgument(WorkRequest workRequest) {}
	
	@Pointcut("target(skaro.pokedex.sdk.worker.command.Command)") 
	private void implementsCommand() {}
	
	@Pointcut("target(skaro.pokedex.sdk.worker.command.validation.ValidationFilter)")
	private void implementsValidationFilter() {}
	
	private Object attemptProceed(ProceedingJoinPoint joinPoint, WorkRequest workRequest) throws Throwable {
		try {
			return joinPoint.proceed(joinPoint.getArgs());
		} catch (Exception e) {
			LOG.error("Caught exception", e);
			
			AnsweredWorkRequest answer = new AnsweredWorkRequest();
			answer.setStatus(WorkStatus.ERROR);
			answer.setWorkRequest(workRequest);
			
			MessageCreateRequest errorResponse = createErrorResponse(workRequest, e);
			
			return Routes.MESSAGE_CREATE.newRequest(workRequest.getChannelId())
				.body(errorResponse)
				.exchange(router)
				.mono()
				.thenReturn(answer);
		}
	}
	
	private MessageCreateRequest createErrorResponse(WorkRequest workRequest, Exception error) {
		EmbedFieldData technicalErrorField = EmbedFieldData.builder()
				.inline(true)
				.name("Technical Error")
				.value(error.getMessage())
				.build();
		EmbedFieldData userInputField = EmbedFieldData.builder()
				.inline(true)
				.name("Command")
				.value(String.format("%s %s", workRequest.getCommmand(), workRequest.getArguments()))
				.build();
		EmbedFieldData supportServerLinkField = EmbedFieldData.builder()
				.inline(true)
				.name("Link to Support Server")
				.value("[Support Server link](https://discord.gg/D5CfFkN)")
				.build();
		
		EmbedData embed = EmbedData.builder().from(MessageEmbedTemplates.ERROR_MESSAGE)
				.description("Some unexpected error occured while processing your request. Please report this error to the support server __as a screenshot__.")
				.addField(technicalErrorField)
				.addField(userInputField)
				.addField(supportServerLinkField)
				.build();
		
		return MessageCreateRequest.builder()
				.embed(embed)
				.build();
	}
}
