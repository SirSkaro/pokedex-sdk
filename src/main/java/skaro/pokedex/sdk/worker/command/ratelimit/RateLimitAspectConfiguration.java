package skaro.pokedex.sdk.worker.command.ratelimit;

import static skaro.pokedex.sdk.worker.command.DefaultWorkerCommandConfiguration.RATE_LIMIT_ASPECT_ORDER;

import java.util.concurrent.TimeUnit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import io.github.bucket4j.AsyncBucket;
import io.github.bucket4j.ConsumptionProbe;
import reactor.core.publisher.Mono;
import skaro.pokedex.sdk.discord.DiscordMessageDirector;
import skaro.pokedex.sdk.messaging.dispatch.AnsweredWorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkStatus;

@Aspect
@Configuration
@Order(RATE_LIMIT_ASPECT_ORDER)
public class RateLimitAspectConfiguration {
	private BucketPool bucketPool;
	private DiscordMessageDirector<RateLimitMessageContent> messageDirector;

	public RateLimitAspectConfiguration(BucketPool bucketPool, DiscordMessageDirector<RateLimitMessageContent> messageDirector) {
		this.bucketPool = bucketPool;
		this.messageDirector = messageDirector;
	}

	@Around("implementsCommand()"
			+ " && classAnnotatedWithRateLimit(rateLimit)"
			+ " && methodHasWorkRequestArgument(workRequest)")
	public Object limit(ProceedingJoinPoint joinPoint, RateLimit rateLimit, WorkRequest workRequest) {
		return getBucketForCommand(rateLimit, workRequest)
				.flatMap(this::probeRateLimit)
				.flatMap(probe -> proceedIfNotRateLimited(joinPoint, rateLimit, workRequest, probe));
	}

	@Pointcut("target(skaro.pokedex.sdk.worker.command.Command)") 
	private void implementsCommand() {}
	@Pointcut("@within(rateLimit)")
	private void classAnnotatedWithRateLimit(RateLimit rateLimit) {}
	@Pointcut("args(workRequest,..)")
	private void methodHasWorkRequestArgument(WorkRequest workRequest) {}

	private Mono<AsyncBucket> getBucketForCommand(RateLimit rateLimit, WorkRequest workRequest) {
		return bucketPool.getBucket(workRequest.getGuildId(), rateLimit);
	}
	
	private Mono<ConsumptionProbe> probeRateLimit(AsyncBucket bucket) {
		return Mono.fromFuture(bucket.tryConsumeAndReturnRemaining(1));
	}
	
	private Mono<AnsweredWorkRequest> proceedIfNotRateLimited(ProceedingJoinPoint joinPoint, RateLimit rateLimit, WorkRequest workRequest, ConsumptionProbe probe) {
		if(probe.isConsumed()) {
			return Mono.defer(() -> proceed(joinPoint));
		}
		return Mono.defer(() -> createRateLimitMessage(rateLimit, workRequest, probe));
	}
	
	@SuppressWarnings("unchecked")
	private Mono<AnsweredWorkRequest> proceed(ProceedingJoinPoint joinPoint) {
		try {
			return ((Mono<AnsweredWorkRequest>)joinPoint.proceed(joinPoint.getArgs()));
		} catch(Throwable e) {
			return Mono.error(e);
		}
	} 
	
	private Mono<AnsweredWorkRequest> createRateLimitMessage(RateLimit rateLimit, WorkRequest workRequest, ConsumptionProbe probe) {
		RateLimitMessageContent messageContent = createMessageContent(rateLimit, workRequest, probe);
		
		return messageDirector.createDiscordMessage(messageContent, workRequest.getChannelId())
				.thenReturn(createAnswer(workRequest));
	}
	
	private RateLimitMessageContent createMessageContent(RateLimit rateLimit, WorkRequest workRequest, ConsumptionProbe probe) {
		long timeLeftInSeconds = TimeUnit.SECONDS.convert(probe.getNanosToWaitForRefill(), TimeUnit.NANOSECONDS);
		
		RateLimitMessageContent messageContent = new RateLimitMessageContent();
		messageContent.setRateLimit(rateLimit);
		messageContent.setLanguage(workRequest.getLanguage());
		messageContent.setCommand(workRequest.getCommmand());
		messageContent.setTimeLeftInSeconds(timeLeftInSeconds);
		
		return messageContent;
	}
	
	private AnsweredWorkRequest createAnswer(WorkRequest workRequest) {
		AnsweredWorkRequest answer = new AnsweredWorkRequest();
		answer.setStatus(WorkStatus.RATE_LIMIT);
		answer.setWorkRequest(workRequest);
		
		return answer;
	}

}
