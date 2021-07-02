package skaro.pokedex.sdk.worker.command.ratelimit;

import java.time.Duration;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Refill;
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
	public static final String WARNING_MESSSAGE_BANDWIDTH_BEAN = "warningMessageBandwidth";
	public static final String COMMAND_BUCKET_POOL_BEAN = "commandRateLimitBucketPool";
	public static final String WARNING_MESSAGE_BUCKET_POOL_BEAN = "warningMessageLimitBucketPool";
	
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
	
	@Bean(WARNING_MESSSAGE_BANDWIDTH_BEAN)
	public Bandwidth warningMessageBandwidth() {
		Refill refill = Refill.intervally(1, Duration.ofSeconds(5));
		return Bandwidth.classic(1, refill);
	}
	
	@Bean
	public RateLimitFacade rateLimitFacade(
			@Qualifier(COMMAND_BUCKET_POOL_BEAN) BucketPool commandBucketPool,
			@Qualifier(WARNING_MESSAGE_BUCKET_POOL_BEAN) BucketPool messageBucketPool,
			@Qualifier(WARNING_MESSSAGE_BANDWIDTH_BEAN) Bandwidth warningMessageBandwidth) {
		return new DualPoolRateLimitFacade(commandBucketPool, messageBucketPool, warningMessageBandwidth);
	}
	
}
