package skaro.pokedex.sdk.cache;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Scheduler;

import reactor.core.publisher.Mono;

@Configuration
public class NearCacheResourceConfiguration {
	public static final String CACHE_MAINTENANCE_SCHEDULER_BEAN = "cacheMaintenanceSchedulerBean";
	
	@Bean(CACHE_MAINTENANCE_SCHEDULER_BEAN)
	public Scheduler scheduler(reactor.core.scheduler.Scheduler scheduler) {
		return (executor, runnable, delay, unit) -> {
			return Mono.delay(Duration.of(delay, unit.toChronoUnit()), scheduler)
				.flatMap(waitTime -> Mono.fromRunnable(runnable))
				.toFuture();
		};
	}
	
}
