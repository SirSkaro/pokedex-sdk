package skaro.pokedex.sdk.cache;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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

import reactor.core.Disposable;


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
			Disposable disposable = scheduler.schedule(runnable);
			return futureFromDisposable(disposable);
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
	
	private Future<Void> futureFromDisposable(Disposable disposable) {
		return new Future<Void>() {
			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				return false;
			}

			@Override
			public Void get() throws InterruptedException, ExecutionException {
				return null;
			}

			@Override
			public Void get(long arg0, TimeUnit arg1) throws InterruptedException, ExecutionException, TimeoutException {
				return null;
			}

			@Override
			public boolean isCancelled() {
				return disposable.isDisposed();
			}

			@Override
			public boolean isDone() {
				return true;
			}
		};
	}
	
}
