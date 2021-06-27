package skaro.pokedex.sdk.cache;

import javax.validation.Valid;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientConnectionStrategyConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spring.cache.HazelcastCacheManager;

@Configuration
public class DistributedCacheConfiguration {
	public static final String DISTRIBUTED_CACHE_MANAGER_BEAN = "distributedCacheManager";
	private static final String DISTRIBUTED_CHACHE_CONFIGURATION_PROPERTIES_PREFIX = "skaro.pokedex.cache.distributed";
	
	@Bean
	@Valid
	@ConfigurationProperties(DISTRIBUTED_CHACHE_CONFIGURATION_PROPERTIES_PREFIX)
	public DistributedCacheConfigurationProperties cacheProperties() {
		return new DistributedCacheConfigurationProperties();
	}
	
	@Bean
	public ClientConfig config(DistributedCacheConfigurationProperties properties) {
		ClientConfig clientConfig= new ClientConfig();
		clientConfig.setClusterName(properties.getClusterName());
		clientConfig.getNetworkConfig()
			.setAddresses(properties.getNodeAddresses());
		clientConfig.getConnectionStrategyConfig()
			.setAsyncStart(true)
			.setReconnectMode(ClientConnectionStrategyConfig.ReconnectMode.ASYNC);
		return clientConfig;
	}
	
	@Bean
	public HazelcastInstance hazelcastInstance(ClientConfig clientConifg) {
		return HazelcastClient.newHazelcastClient(clientConifg);
	}
	
	@Bean(DISTRIBUTED_CACHE_MANAGER_BEAN)
	public CacheManager cacheManager(HazelcastInstance hazelcastInstance) {
		HazelcastCacheManager cacheManager = new HazelcastCacheManager(hazelcastInstance);
		return cacheManager;
	}
	
}
