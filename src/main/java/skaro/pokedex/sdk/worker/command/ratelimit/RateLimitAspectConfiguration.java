package skaro.pokedex.sdk.worker.command.ratelimit;

import static skaro.pokedex.sdk.worker.command.DefaultWorkerCommandConfiguration.RATE_LIMIT_ASPECT_ORDER;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import reactor.core.publisher.Mono;
import skaro.pokedex.sdk.discord.DiscordMessageDirector;
import skaro.pokedex.sdk.messaging.dispatch.AnsweredWorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkStatus;

@Aspect
@Configuration
@Order(RATE_LIMIT_ASPECT_ORDER)
public class RateLimitAspectConfiguration {
	private RateLimitFacade rateLimitFacade;
	private DiscordMessageDirector<RateLimitMessageContent> messageDirector;

	public RateLimitAspectConfiguration(RateLimitFacade rateLimitFacade, DiscordMessageDirector<RateLimitMessageContent> messageDirector) {
		this.rateLimitFacade = rateLimitFacade;
		this.messageDirector = messageDirector;
	}

	@Around("implementsCommand()"
			+ " && classAnnotatedWithRateLimit(rateLimit)"
			+ " && methodHasWorkRequestArgument(workRequest)")
	public Object limit(ProceedingJoinPoint joinPoint, RateLimit rateLimit, WorkRequest workRequest) {
		return rateLimitFacade.isCommandOnCooldownForGuild(workRequest.getGuildId(), rateLimit)
			.flatMap(cooldownReport -> cooldownReport.isOnCooldown() 
					? createRateLimitMessage(rateLimit, workRequest, cooldownReport)
					: proceedWithCommand(joinPoint));
	}

	@Pointcut("target(skaro.pokedex.sdk.worker.command.Command)") 
	private void implementsCommand() {}
	@Pointcut("@within(rateLimit)")
	private void classAnnotatedWithRateLimit(RateLimit rateLimit) {}
	@Pointcut("args(workRequest,..)")
	private void methodHasWorkRequestArgument(WorkRequest workRequest) {}

	private Mono<AnsweredWorkRequest> createRateLimitMessage(RateLimit rateLimit, WorkRequest workRequest, CooldownReport report) {
		return Mono.just(createMessageContent(rateLimit, workRequest, report))
			.filterWhen(messageContent -> shouldSendCooldownMessage(rateLimit, workRequest))
			.flatMap(messageContent -> messageDirector.createDiscordMessage(messageContent, workRequest.getChannelId()))
			.thenReturn(createAnswer(workRequest));
	}
	
	@SuppressWarnings("unchecked")
	private Mono<AnsweredWorkRequest> proceedWithCommand(ProceedingJoinPoint joinPoint) {
		try {
			return ((Mono<AnsweredWorkRequest>)joinPoint.proceed(joinPoint.getArgs()));
		} catch(Throwable e) {
			return Mono.error(e);
		}
	} 
	
	private Mono<Boolean> shouldSendCooldownMessage(RateLimit rateLimit, WorkRequest workRequest) {
		return rateLimitFacade.isWarningMessageOnCooldownForGuild(workRequest.getGuildId(), rateLimit.command())
				.map(report -> !report.isOnCooldown());
	}
	
	private RateLimitMessageContent createMessageContent(RateLimit rateLimit, WorkRequest workRequest, CooldownReport report) {
		RateLimitMessageContent messageContent = new RateLimitMessageContent();
		messageContent.setRateLimit(rateLimit);
		messageContent.setLanguage(workRequest.getLanguage());
		messageContent.setCommand(workRequest.getCommmand());
		messageContent.setTimeLeftInSeconds(report.getSecondsLeftInCooldown());
		
		return messageContent;
	}
	
	private AnsweredWorkRequest createAnswer(WorkRequest workRequest) {
		AnsweredWorkRequest answer = new AnsweredWorkRequest();
		answer.setStatus(WorkStatus.RATE_LIMIT);
		answer.setWorkRequest(workRequest);
		
		return answer;
	}

}
