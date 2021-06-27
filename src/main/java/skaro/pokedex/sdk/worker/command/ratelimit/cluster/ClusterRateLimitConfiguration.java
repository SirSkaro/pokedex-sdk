package skaro.pokedex.sdk.worker.command.ratelimit.cluster;

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
	private static final String RATE_LIMIT_MAP_NAME = "per-guild-bucket-map";
	
	@Bean
	public MapConfig mapConfig(ApplicationContext context) {
		int maxTimeToLive = context.getBeansWithAnnotation(RateLimit.class).entrySet().stream()
				.map(beanEntry -> beanEntry.getValue())
				.map(bean -> AnnotationUtils.findAnnotation(bean.getClass(), RateLimit.class))
				.map(RateLimit::seconds)
				.max(Integer::compare)
				.orElse(60);
		
		MapConfig config = new MapConfig(RATE_LIMIT_MAP_NAME);
		config.setTimeToLiveSeconds(maxTimeToLive);
	
		return config;
	}
	
	@Bean
	public ProxyManager<String> proxyManager(HazelcastInstance hazelcastInstance, MapConfig mapConfig) {
		hazelcastInstance.getConfig().addMapConfig(mapConfig);
		return Bucket4j.extension(Hazelcast.class)
				.proxyManagerForMap(hazelcastInstance.getMap(RATE_LIMIT_MAP_NAME));
	}
	
	@Bean
	public BucketPool bucketPool(ProxyManager<String> proxyManger) {
		return new ClusteredBucketPool(proxyManger);
	}

}
