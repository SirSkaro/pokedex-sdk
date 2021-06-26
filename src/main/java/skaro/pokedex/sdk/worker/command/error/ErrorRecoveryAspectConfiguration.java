package skaro.pokedex.sdk.worker.command.error;

import static skaro.pokedex.sdk.worker.command.DefaultWorkerCommandConfiguration.ERROR_RECOVERY_ASPECT_ORDER;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

import reactor.core.publisher.Mono;
import skaro.pokedex.sdk.discord.DiscordMessageDirector;
import skaro.pokedex.sdk.messaging.dispatch.AnsweredWorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkStatus;

@Aspect
@Configuration
@Order(ERROR_RECOVERY_ASPECT_ORDER)
@Import(ErrorMessageDirectorConfiguration.class)
public class ErrorRecoveryAspectConfiguration {
	private DiscordMessageDirector<ErrorMessageContent> messageDirector;

	public ErrorRecoveryAspectConfiguration(DiscordMessageDirector<ErrorMessageContent> messageDirector) {
		this.messageDirector = messageDirector;
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
	
	@SuppressWarnings("unchecked")
	private Object attemptProceed(ProceedingJoinPoint joinPoint, WorkRequest workRequest) throws Throwable {
			return ((Mono<AnsweredWorkRequest>)joinPoint.proceed(joinPoint.getArgs()))
					.onErrorResume(error -> sendErrorMessage(workRequest, error) );
	}
	
	private Mono<AnsweredWorkRequest> sendErrorMessage(WorkRequest workRequest, Throwable error) {
		ErrorMessageContent messageContent = new ErrorMessageContent();
		messageContent.setWorkRequest(workRequest);
		messageContent.setError(error);
		error.printStackTrace();
		
		return messageDirector.createDiscordMessage(messageContent, workRequest.getChannelId())
				.thenReturn(createAnswer(workRequest));
	}
	
	private AnsweredWorkRequest createAnswer(WorkRequest workRequest) {
		AnsweredWorkRequest answer = new AnsweredWorkRequest();
		answer.setStatus(WorkStatus.ERROR);
		answer.setWorkRequest(workRequest);
		
		return answer;
	}
	
}
