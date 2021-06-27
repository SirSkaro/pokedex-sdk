package skaro.pokedex.sdk.worker.command.specification;

import java.util.HashMap;
import java.util.stream.Stream;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import skaro.pokedex.sdk.client.Language;
import skaro.pokedex.sdk.discord.DiscordMessageDirector;
import skaro.pokedex.sdk.discord.DiscordRouterFacade;
import skaro.pokedex.sdk.discord.MessageCreateRequestDirector;
import skaro.pokedex.sdk.worker.command.validation.common.DiscordPermissionsMessageBuilder;
import skaro.pokedex.sdk.worker.command.validation.common.DiscordPermissionsMessageContent;

@Configuration
public class CommonLocaleSpecConfiguration {
	public static final String ERROR_LOCALE_SPEC_BEAN = "errorMessageLocaleSpecBean";
	private static final String ERROR_LOCALE_SPEC_PROPERTIES_PREFIX = "skaro.pokedex.sdk.discord.embed-locale.error";

	public static final String BASE_WARNING_LOCALE_SPEC_BEAN = "warningMessageLocaleSpecBean";
	private static final String WARNING_LOCALE_SPEC_PROPERTIES_PREFIX = "skaro.pokedex.sdk.discord.embed-locale.warning";
	
	public static final String DISOCRD_PERMISSION_LOCALE_SPEC_BEAN = "discordPermissionLocaleSpecBean";
	private static final String BASE_DISCORD_PERMISSION_LOCALE_SPEC_BEAN = "baseDiscordPermissionLocaleSpecBean";
	private static final String DISOCRD_PERMISSION_LOCALE_SPEC_PROPERTIES_PREFIX = "skaro.pokedex.sdk.discord.embed-locale.filter.discord-permissions";
	public static final String DISCORD_PERMISSION_MESSAGE_DIRECTOR_BEAN = "discordPermissionMessageDIrectorBean";
	
	public static final String BASE_EXPECTED_ARGUMENTS_FILTER_LOCALE_SPEC_BEAN = "baseExpectedArgumentsFilterLocaleSpecBean";
	public static final String EXPECTED_ARGUMENTS_FILTER_LOCALE_SPEC_BEAN = "expectedArgumentsFilterLocaleSpecBean";
	private static final String BASE_EXPECTED_ARGUMENTS_FILTER_LOCALE_SPEC_PROPERTIES_PREFIX = "skaro.pokedex.sdk.discord.embed-locale.filter.expected-arguments";
	
	@Bean(ERROR_LOCALE_SPEC_BEAN)
	@ConfigurationProperties(ERROR_LOCALE_SPEC_PROPERTIES_PREFIX)
	@Valid
	public DiscordEmbedLocaleSpec errorMessageLocaleSpec() {
		return new DiscordEmbedLocaleSpec();
	}
	
	@Bean(BASE_WARNING_LOCALE_SPEC_BEAN)
	@ConfigurationProperties(WARNING_LOCALE_SPEC_PROPERTIES_PREFIX)
	@Valid
	public DiscordEmbedLocaleSpec warningMessageLocaleSpec() {
		return new DiscordEmbedLocaleSpec();
	}
	
	@Bean(BASE_DISCORD_PERMISSION_LOCALE_SPEC_BEAN)
	@Valid
	@ConfigurationProperties(DISOCRD_PERMISSION_LOCALE_SPEC_PROPERTIES_PREFIX)
	public DiscordEmbedLocaleSpec baseInvalidDiscordPermissionsMessageLocaleSpec() {
		return new DiscordEmbedLocaleSpec();
	}
	
	@Bean(DISOCRD_PERMISSION_LOCALE_SPEC_BEAN)
	@Valid
	public DiscordEmbedLocaleSpec invalidDiscordPermissionMessageLocaleSpec(
			@Qualifier(BASE_WARNING_LOCALE_SPEC_BEAN) DiscordEmbedLocaleSpec baseWarningLocaleSpec,
			@Qualifier(BASE_DISCORD_PERMISSION_LOCALE_SPEC_BEAN) DiscordEmbedLocaleSpec basePermissionsLocaleSpec) {
		DiscordEmbedLocaleSpec result = new DiscordEmbedLocaleSpec();
		result.setColor(baseWarningLocaleSpec.getColor());
		result.setThumbnail(baseWarningLocaleSpec.getThumbnail());
		result.setEmbedSpecs(new HashMap<>());
		Stream.of(Language.values())
			.forEach(language -> {
				DiscordEmbedSpec spec = new DiscordEmbedSpec();
				spec.setTitle(baseWarningLocaleSpec.getEmbedSpecs().get(language).getTitle());
				spec.setDescription(basePermissionsLocaleSpec.getEmbedSpecs().get(language).getDescription());
				result.getEmbedSpecs().put(language, spec);
			});
		return result;
	}
	
	@Bean(DISCORD_PERMISSION_MESSAGE_DIRECTOR_BEAN)
	public DiscordMessageDirector<DiscordPermissionsMessageContent> discordPermissionMessageDirector(
			DiscordRouterFacade router, 
			@Qualifier(DISOCRD_PERMISSION_LOCALE_SPEC_BEAN) DiscordEmbedLocaleSpec localeSpec) {
		DiscordPermissionsMessageBuilder messageBuilder = new DiscordPermissionsMessageBuilder(localeSpec);
		return new MessageCreateRequestDirector<DiscordPermissionsMessageContent>(router, messageBuilder);
	}
	
	@Bean(BASE_EXPECTED_ARGUMENTS_FILTER_LOCALE_SPEC_BEAN)
	@Valid
	@ConfigurationProperties(BASE_EXPECTED_ARGUMENTS_FILTER_LOCALE_SPEC_PROPERTIES_PREFIX)
	public DiscordEmbedLocaleSpec baseExpectedArgumentsFilterLocaleSpec() {
		return new DiscordEmbedLocaleSpec();
	}
	
	@Bean(EXPECTED_ARGUMENTS_FILTER_LOCALE_SPEC_BEAN)
	@Valid
	public DiscordEmbedLocaleSpec baseExpectedArgumentsFilterLocaleSpec(
			@Qualifier(BASE_WARNING_LOCALE_SPEC_BEAN) DiscordEmbedLocaleSpec baseWarningLocaleSpec,
			@Qualifier(BASE_EXPECTED_ARGUMENTS_FILTER_LOCALE_SPEC_BEAN) DiscordEmbedLocaleSpec baseExpectedArgumentsLocaleSpec) {
		DiscordEmbedLocaleSpec result = new DiscordEmbedLocaleSpec();
		result.setColor(baseWarningLocaleSpec.getColor());
		result.setThumbnail(baseWarningLocaleSpec.getThumbnail());
		result.setEmbedSpecs(new HashMap<>());
		Stream.of(Language.values()).forEach(language -> {
			DiscordEmbedSpec spec = new DiscordEmbedSpec();
			spec.setTitle(baseWarningLocaleSpec.getEmbedSpecs().get(language).getTitle());
			spec.setDescription(baseExpectedArgumentsLocaleSpec.getEmbedSpecs().get(language).getDescription());
			result.getEmbedSpecs().put(language, spec);
		});
		return result;
	}
}
