package skaro.pokedex.sdk.worker.command.ratelimit;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import skaro.pokedex.sdk.discord.DiscordMessageDirector;
import skaro.pokedex.sdk.discord.DiscordRouterFacade;
import skaro.pokedex.sdk.discord.MessageBuilder;
import skaro.pokedex.sdk.discord.MessageCreateRequestDirector;
import skaro.pokedex.sdk.worker.command.specification.DiscordEmbedLocaleSpec;

@Configuration
@Import(RateLimitAspectConfiguration.class)
public class BaseRateLimitConfiguration {
	public static final String RATE_LIMIT_LOCALE_SPEC_BEAN = "rateLimitMessageLocaleSpecBean";
	public static final String RATE_LIMIT_MESSAGE_DIRECTOR_BEAN = "rateLimitMessageDirector";
	private static final String RATE_LIMIT_LOCALE_SPEC_PROPERTIES_PREFIX = "skaro.pokedex.sdk.discord.embed-locale.rate-limit";
	
	@Bean(RATE_LIMIT_LOCALE_SPEC_BEAN)
	@ConfigurationProperties(RATE_LIMIT_LOCALE_SPEC_PROPERTIES_PREFIX)
	@Valid
	public DiscordEmbedLocaleSpec errorMessageLocaleSpec() {
		return new DiscordEmbedLocaleSpec();
	}
	
	@Bean(RATE_LIMIT_MESSAGE_DIRECTOR_BEAN)
	public DiscordMessageDirector<RateLimitMessageContent> messageDirector(
			@Qualifier(RATE_LIMIT_LOCALE_SPEC_BEAN) DiscordEmbedLocaleSpec localeSpec,
			DiscordRouterFacade router) {
		MessageBuilder<RateLimitMessageContent> builder = new RateLimitMessageBuilder(localeSpec);
		return new MessageCreateRequestDirector<>(router, builder);
	}
	
}
