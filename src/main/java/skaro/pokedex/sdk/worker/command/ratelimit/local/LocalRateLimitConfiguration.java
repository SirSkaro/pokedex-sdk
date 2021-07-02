package skaro.pokedex.sdk.worker.command.ratelimit.local;

import static skaro.pokedex.sdk.worker.command.ratelimit.BaseRateLimitConfiguration.COMMAND_BUCKET_POOL_BEAN;
import static skaro.pokedex.sdk.worker.command.ratelimit.BaseRateLimitConfiguration.WARNING_MESSAGE_BUCKET_POOL_BEAN;

import java.time.Duration;
import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AnnotationUtils;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Scheduler;

import skaro.pokedex.sdk.cache.NearCacheResourceConfiguration;
import skaro.pokedex.sdk.worker.command.ratelimit.BaseRateLimitConfiguration;
import skaro.pokedex.sdk.worker.command.ratelimit.BucketPool;
import skaro.pokedex.sdk.worker.command.ratelimit.RateLimit;

@Configuration
@Import({
	BaseRateLimitConfiguration.class,
	NearCacheResourceConfiguration.class
})
public class LocalRateLimitConfiguration {
	private static final String RATE_LIMIT_CACHE_BEAN = "rateLimitCache";
	private static final String WARNING_MESSAGE_CACHE_BEAN = "warningMessageCache";
	
	@Bean(RATE_LIMIT_CACHE_BEAN)
	public Cache commandRateLimitCache(ApplicationContext context, 
			Scheduler scheduler, 
			Executor executor) {
		int maxTimeToLive = getLargestRateLimitDuration(context);
		
		com.github.benmanes.caffeine.cache.Cache<Object, Object> cache = Caffeine.newBuilder()
			.expireAfterAccess(Duration.ofSeconds(maxTimeToLive))
			.executor(executor)
			.scheduler(scheduler)
			.build();
		
		return new CaffeineCache("rateLimit", cache);
	}
	
	@Bean(WARNING_MESSAGE_CACHE_BEAN)
	public Cache warningMessageCache(Scheduler scheduler, Executor executor) {
		com.github.benmanes.caffeine.cache.Cache<Object, Object> cache = Caffeine.newBuilder()
				.expireAfterAccess(Duration.ofSeconds(30))
				.executor(executor)
				.scheduler(scheduler)
				.build();
		
		return new CaffeineCache("warningMessage", cache);
	}
	
	@Bean(COMMAND_BUCKET_POOL_BEAN)
	public BucketPool commandBucketPool(@Qualifier(RATE_LIMIT_CACHE_BEAN) Cache cache) {
		return new LocalBucketPool(cache);
	}
	
	@Bean(WARNING_MESSAGE_BUCKET_POOL_BEAN)
	public BucketPool warningMessageBucketPool(@Qualifier(WARNING_MESSAGE_CACHE_BEAN) Cache cache) {
		return new LocalBucketPool(cache);
	}
	
	private int getLargestRateLimitDuration(ApplicationContext context) {
		return context.getBeansWithAnnotation(RateLimit.class).entrySet().stream()
			.map(beanEntry -> beanEntry.getValue())
			.map(bean -> AnnotationUtils.findAnnotation(bean.getClass(), RateLimit.class))
			.map(RateLimit::seconds)
			.max(Integer::compare)
			.orElse(60);
	}
	
}
