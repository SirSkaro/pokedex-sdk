package skaro.pokedex.sdk.worker.command.ratelimit;

import static skaro.pokedex.sdk.worker.command.DefaultWorkerCommandConfiguration.RATE_LIMIT_ASPECT_ORDER;

import java.lang.invoke.MethodHandles;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import io.github.bucket4j.AsyncBucket;
import io.github.bucket4j.ConsumptionProbe;
import reactor.core.publisher.Mono;
import skaro.pokedex.sdk.messaging.dispatch.AnsweredWorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkStatus;

@Aspect
@Configuration
@Order(RATE_LIMIT_ASPECT_ORDER)
public class RateLimitAspectConfiguration {
	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private BucketPool bucketPool;

	public RateLimitAspectConfiguration(BucketPool bucketPool) {
		this.bucketPool = bucketPool;
	}

	@Around("implementsCommand()"
			+ " && classAnnotatedWithRateLimit(rateLimit)"
			+ " && methodHasWorkRequestArgument(workRequest)")
	public Object limit(ProceedingJoinPoint joinPoint, RateLimit rateLimit, WorkRequest workRequest) {
		return proceedIfNotRateLimited(joinPoint, rateLimit, workRequest)
			.switchIfEmpty(Mono.defer(() -> Mono.just(createRatelimitAnswer(workRequest))));
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
	
	private Mono<AnsweredWorkRequest> proceedIfNotRateLimited(ProceedingJoinPoint joinPoint, RateLimit rateLimit, WorkRequest workRequest) {
		return getBucketForCommand(rateLimit, workRequest)
				.flatMap(bucket -> probeRateLimit(bucket))
				.flatMap(probe -> probe.isConsumed() 
					? Mono.defer(() -> proceed(joinPoint)) 
					: Mono.empty());
	}
	
	private Mono<ConsumptionProbe> probeRateLimit(AsyncBucket bucket) {
		return Mono.fromFuture(bucket.tryConsumeAndReturnRemaining(1));
	}
	
	@SuppressWarnings("unchecked")
	private Mono<AnsweredWorkRequest> proceed(ProceedingJoinPoint joinPoint) {
		try {
			return ((Mono<AnsweredWorkRequest>)joinPoint.proceed(joinPoint.getArgs()));
		} catch(Throwable e) {
			return Mono.error(e);
		}
	} 
	
	private AnsweredWorkRequest createRatelimitAnswer(WorkRequest workRequest) {
		LOG.info("rate limited");
		AnsweredWorkRequest answer = new AnsweredWorkRequest();
		answer.setStatus(WorkStatus.RATE_LIMIT);
		answer.setWorkRequest(workRequest);
		
		return answer;
	}

}
