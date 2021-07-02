package skaro.pokedex.sdk.cache;

import java.time.Duration;
import java.util.concurrent.Executor;

import javax.validation.Valid;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Scheduler;

import skaro.pokedex.sdk.client.CacheFacade;
import skaro.pokedex.sdk.client.MonoCacheFacade;


@Configuration
@EnableCaching
@Import(NearCacheResourceConfiguration.class)
public class NearCacheConfiguration {
	public static final String NEAR_CACHE_MANAGER_BEAN = "nearCacheManager";
	private static final String NEAR_CACHE_CONFIGURATION_PROPERTIES_PREFIX = "skaro.pokedex.cache.near";
	
	@Bean
	@ConfigurationProperties(NEAR_CACHE_CONFIGURATION_PROPERTIES_PREFIX)
	@Valid
	public NearCacheConfigurationProperties cacheConfigurationProperties() {
		return new NearCacheConfigurationProperties();
	}
	
	@Bean
	public Caffeine<Object, Object> caffeineConfig(Executor executor, Scheduler scheduler, NearCacheConfigurationProperties cacheProperties) {
		return Caffeine.newBuilder()
	    		.executor(executor)
	    		.scheduler(scheduler)
	    		.expireAfterAccess(Duration.ofMinutes(cacheProperties.getTimeToLive()))
	    		.maximumSize(cacheProperties.getMaxSize());
	}
	
	@Bean(NEAR_CACHE_MANAGER_BEAN)
	@Primary
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
