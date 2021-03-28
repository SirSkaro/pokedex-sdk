package skaro.pokedex.sdk.worker;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.netty.resolver.DefaultAddressResolverGroup;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WorkerResourceConfiguration {

	@Bean
	public Executor executor() {
		int availableThreadCount = Runtime.getRuntime().availableProcessors() * 2;
		return Executors.newFixedThreadPool(availableThreadCount);
	}
	
	@Bean
	public Scheduler scheduler(Executor executor) {
		return Schedulers.fromExecutor(executor);
	}
	
	@Bean
	public HttpClient httpClient() {
		return HttpClient.create()
				.compress(true)
				.followRedirect(true)
				.secure()
				.resolver(DefaultAddressResolverGroup.INSTANCE);
	}
	
}
