package skaro.pokedex.sdk.cache;

import javax.validation.Valid;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientConnectionStrategyConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

@Configuration
public class DistributedCacheConfiguration {
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
	
//	@Bean
//	public HazelcastInstance hazelcastInstance(ClientConfig clientConifg) {
//		return HazelcastClient.newHazelcastClient(clientConifg);
//	}
	
}
