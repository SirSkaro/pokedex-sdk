package skaro.pokedex.sdk.worker.command.ratelimit.local;

import static skaro.pokedex.sdk.cache.NearCacheConfiguration.CACHE_MAINTENANCE_SCHEDULER_BEAN;

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

import skaro.pokedex.sdk.cache.NearCacheConfiguration;
import skaro.pokedex.sdk.worker.command.ratelimit.BucketPool;
import skaro.pokedex.sdk.worker.command.ratelimit.RateLimit;
import skaro.pokedex.sdk.worker.command.ratelimit.RateLimitAspectConfiguration;

@Configuration
@Import({
	RateLimitAspectConfiguration.class,
	NearCacheConfiguration.class
})
public class RateLimitConfiguration {
	private final static String RATE_LIMIT_CACHE_BEAN = "rateLimitCache";
	
	@Bean(RATE_LIMIT_CACHE_BEAN)
	public Cache rateLimitCache(ApplicationContext context, 
			@Qualifier(CACHE_MAINTENANCE_SCHEDULER_BEAN) Scheduler scheduler, 
			Executor executor) {
		int maxTimeToLive = getLargestRateLimitDuration(context);
		
		com.github.benmanes.caffeine.cache.Cache<Object, Object> cache = Caffeine.newBuilder()
			.expireAfterAccess(Duration.ofSeconds(maxTimeToLive))
			.executor(executor)
			.scheduler(scheduler)
			.build();
		
		return new CaffeineCache("rateLimit", cache);
	}
	
	@Bean
	public BucketPool bucketPool(@Qualifier(RATE_LIMIT_CACHE_BEAN) Cache cache) {
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
