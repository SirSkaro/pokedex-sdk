package skaro.pokedex.sdk;

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

@Configuration
public class WorkerDiscordConfiguration {

	@Bean
	@ConfigurationProperties(DiscordConfigurationProperties.DISCORD_PROPERTIES_PREFIX)
	@Valid
	public DiscordConfigurationProperties getDiscordConfigurationProperties() {
		return new DiscordConfigurationProperties();
	}
	
	@Bean
	public RouterOptions getRouterOptions(DiscordConfigurationProperties discordProperties) {
		ExchangeStrategies exchangeStrategies = ExchangeStrategies.jackson(JacksonResources.create().getObjectMapper());
		
		return new RouterOptions(
				discordProperties.getToken(),
				ReactorResources.create(),
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
