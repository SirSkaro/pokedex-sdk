package skaro.pokedex.sdk.worker.command.ratelimit.cluster;

import static skaro.pokedex.sdk.worker.command.ratelimit.BaseRateLimitConfiguration.COMMAND_BUCKET_POOL_BEAN;
import static skaro.pokedex.sdk.worker.command.ratelimit.BaseRateLimitConfiguration.WARNING_MESSAGE_BUCKET_POOL_BEAN;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AnnotationUtils;

import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastInstance;

import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.grid.ProxyManager;
import io.github.bucket4j.grid.hazelcast.Hazelcast;
import skaro.pokedex.sdk.cache.DistributedCacheConfiguration;
import skaro.pokedex.sdk.worker.command.ratelimit.BaseRateLimitConfiguration;
import skaro.pokedex.sdk.worker.command.ratelimit.BucketPool;
import skaro.pokedex.sdk.worker.command.ratelimit.RateLimit;

@Configuration
@Import({
	DistributedCacheConfiguration.class,
	BaseRateLimitConfiguration.class
})
public class ClusterRateLimitConfiguration {
	private static final String COMMAND_RATE_LIMIT_MAP_CONFIG_BEAN = "commandRateLimitMapConfig";
	private static final String WARNING_MESSAGE_MAP_CONFIG_BEAN = "warningMessageMapConfig";
	private static final String COMMAND_RATE_LIMIT_MAP = "per-command-per-guild-bucket-map";
	private static final String WARNING_MESSAGE_RATE_LIMIT_MAP = "per-guild-warning-message-bucket-map";
	private static final String COMMAND_RATE_LIMIT_PROXY_MANAGER_BEAN = "commandRateLimitProxyManager";
	private static final String WARNING_MESSAGE_PROXY_MANAGER_BEAN = "warningMessageProxyManager";
	
	@Bean(COMMAND_RATE_LIMIT_MAP_CONFIG_BEAN)
	public MapConfig commandRateLimitMapConfig(ApplicationContext context) {
		int maxTimeToLive = context.getBeansWithAnnotation(RateLimit.class).entrySet().stream()
				.map(beanEntry -> beanEntry.getValue())
				.map(bean -> AnnotationUtils.findAnnotation(bean.getClass(), RateLimit.class))
				.map(RateLimit::seconds)
				.max(Integer::compare)
				.orElse(60);
		
		MapConfig config = new MapConfig(COMMAND_RATE_LIMIT_MAP);
		config.setTimeToLiveSeconds(maxTimeToLive);
	
		return config;
	}
	
	@Bean(WARNING_MESSAGE_MAP_CONFIG_BEAN)
	public MapConfig warningMessageMapConfig() {
		MapConfig config = new MapConfig(WARNING_MESSAGE_RATE_LIMIT_MAP);
		config.setTimeToLiveSeconds(10);
		
		return config;
	}
	
	@Bean(COMMAND_RATE_LIMIT_PROXY_MANAGER_BEAN)
	public ProxyManager<String> commandRateLimitProxyManager(
			HazelcastInstance hazelcastInstance, 
			@Qualifier(COMMAND_RATE_LIMIT_MAP_CONFIG_BEAN) MapConfig mapConfig) {
		hazelcastInstance.getConfig().addMapConfig(mapConfig);
		return Bucket4j.extension(Hazelcast.class)
				.proxyManagerForMap(hazelcastInstance.getMap(COMMAND_RATE_LIMIT_MAP));
	}
	
	@Bean(WARNING_MESSAGE_PROXY_MANAGER_BEAN)
	public ProxyManager<String> warningMessageProxyManager(
			HazelcastInstance hazelcastInstance, 
			@Qualifier(WARNING_MESSAGE_MAP_CONFIG_BEAN) MapConfig mapConfig) {
		hazelcastInstance.getConfig().addMapConfig(mapConfig);
		return Bucket4j.extension(Hazelcast.class)
				.proxyManagerForMap(hazelcastInstance.getMap(WARNING_MESSAGE_RATE_LIMIT_MAP));
	}
	
	@Bean(COMMAND_BUCKET_POOL_BEAN)
	public BucketPool commandBucketPool(@Qualifier(COMMAND_RATE_LIMIT_PROXY_MANAGER_BEAN) ProxyManager<String> proxyManger) {
		return new ClusteredBucketPool(proxyManger);
	}
	
	@Bean(WARNING_MESSAGE_BUCKET_POOL_BEAN)
	public BucketPool warningMessageBucketPool(@Qualifier(WARNING_MESSAGE_PROXY_MANAGER_BEAN) ProxyManager<String> proxyManger) {
		return new ClusteredBucketPool(proxyManger);
	}

}
