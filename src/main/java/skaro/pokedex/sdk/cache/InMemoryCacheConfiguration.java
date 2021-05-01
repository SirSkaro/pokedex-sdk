package skaro.pokedex.sdk.cache;

import java.time.Duration;
import java.util.concurrent.Executor;

import javax.validation.Valid;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Scheduler;

import reactor.core.publisher.Mono;
import skaro.pokedex.sdk.client.CacheFacade;
import skaro.pokedex.sdk.client.MonoCacheFacade;


@Configuration
@EnableCaching
public class InMemoryCacheConfiguration {
	private static final String CACHE_CONFIGURATION_PROPERTIES_PREFIX = "skaro.pokedex.cache";
	public static final String CACHE_MAINTENANCE_SCHEDULER_BEAN = "cacheMaintenanceSchedulerBean";
	
	@Bean
	@ConfigurationProperties(CACHE_CONFIGURATION_PROPERTIES_PREFIX)
	@Valid
	public CacheConfigurationProperties cacheConfigurationProperties() {
		return new CacheConfigurationProperties();
	}
	
	@Bean(CACHE_MAINTENANCE_SCHEDULER_BEAN)
	public Scheduler scheduler(reactor.core.scheduler.Scheduler scheduler) {
		return (executor, runnable, delay, unit) -> {
			return Mono.delay(Duration.of(delay, unit.toChronoUnit()), scheduler)
				.flatMap(waitTime -> Mono.fromRunnable(runnable))
				.toFuture();
		};
	}
	
	@Bean
	public @NonNull Caffeine<Object, Object> caffeineConfig(Executor executor, Scheduler scheduler, CacheConfigurationProperties cacheProperties) {
		return Caffeine.newBuilder()
	    		.executor(executor)
	    		.scheduler(scheduler)
	    		.expireAfterAccess(Duration.ofMinutes(cacheProperties.getTimeToLive()))
	    		.maximumSize(cacheProperties.getMaxSize());
	}
	
	@Bean
	public CacheManager cacheManager(Caffeine<Object, Object> caffeine) {
	    CaffeineCacheManager cacheManager = new CaffeineCacheManager();
	    cacheManager.setCaffeine(caffeine);
	    return cacheManager;
	}
	
	@Bean
	public CacheFacade cacheFacade(CacheManager cacheManager) {
		return new MonoCacheFacade(cacheManager);
	}
	
}
