package skaro.pokedex.sdk.worker;

import java.util.Collections;

import javax.validation.Valid;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import discord4j.common.JacksonResources;
import discord4j.common.ReactorResources;
import discord4j.rest.http.ExchangeStrategies;
import discord4j.rest.request.BucketGlobalRateLimiter;
import discord4j.rest.request.DefaultRouter;
import discord4j.rest.request.RequestQueueFactory;
import discord4j.rest.request.Router;
import discord4j.rest.request.RouterOptions;
import io.netty.resolver.DefaultAddressResolverGroup;
import reactor.core.scheduler.Scheduler;
import reactor.netty.http.client.HttpClient;
import skaro.pokedex.sdk.DiscordConfigurationProperties;

@Configuration
public class WorkerDiscordConfiguration {

	@Bean
	@ConfigurationProperties(DiscordConfigurationProperties.DISCORD_PROPERTIES_PREFIX)
	@Valid
	public DiscordConfigurationProperties getDiscordConfigurationProperties() {
		return new DiscordConfigurationProperties();
	}
	
	@Bean
	public RouterOptions getRouterOptions(DiscordConfigurationProperties discordProperties, Scheduler scheduler) {
		ExchangeStrategies exchangeStrategies = ExchangeStrategies.jackson(JacksonResources.create().getObjectMapper());
		
		/*
		 * Current workaround to creating ReactorResources. Instead of using ReactorResources.create(),
		 * we need to make a custom HttpClient. Otherwise requests time out after the default 5000 miliseconds
		 * before being able to resolve.
		 * 
		 * https://github.com/reactor/reactor-netty/issues/1431
		 */
		HttpClient httpClient = ReactorResources.DEFAULT_HTTP_CLIENT.get()
				.resolver(DefaultAddressResolverGroup.INSTANCE);
		ReactorResources reactorResources = ReactorResources.builder()
			.timerTaskScheduler(scheduler)
			.httpClient(httpClient)
			.build();
		
		return new RouterOptions(
				discordProperties.getToken(),
				reactorResources,
				exchangeStrategies,
				Collections.emptyList(),
				BucketGlobalRateLimiter.create(),
				RequestQueueFactory.buffering()
		);
	}
	
	@Bean
	public Router getDiscordRestRouter(RouterOptions routerOptions) {
		return new DefaultRouter(routerOptions);
	}
	
}
