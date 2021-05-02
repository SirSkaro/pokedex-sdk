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
import reactor.core.scheduler.Scheduler;
import reactor.netty.http.client.HttpClient;
import skaro.pokedex.sdk.DiscordConfigurationProperties;
import skaro.pokedex.sdk.discord.Discord4jRouterFacade;
import skaro.pokedex.sdk.discord.DiscordRouterFacade;

@Configuration
public class WorkerDiscordConfiguration {

	@Bean
	@ConfigurationProperties(DiscordConfigurationProperties.DISCORD_PROPERTIES_PREFIX)
	@Valid
	public DiscordConfigurationProperties getDiscordConfigurationProperties() {
		return new DiscordConfigurationProperties();
	}
	
	@Bean
	public ReactorResources reactorResources(Scheduler scheduler, HttpClient httpClient) {
		return ReactorResources.builder()
			.timerTaskScheduler(scheduler)
			.httpClient(httpClient)
			.build();
	}
	
	@Bean
	public RouterOptions getRouterOptions(DiscordConfigurationProperties discordProperties, ReactorResources reactorResources) {
		ExchangeStrategies exchangeStrategies = ExchangeStrategies.jackson(JacksonResources.create().getObjectMapper());
		
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
	
	@Bean
	public DiscordRouterFacade discordRouterFacade(Router router) {
		return new Discord4jRouterFacade(router);
	}
	
}
