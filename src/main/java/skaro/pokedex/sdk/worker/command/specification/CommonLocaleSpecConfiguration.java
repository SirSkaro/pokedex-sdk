package skaro.pokedex.sdk.worker.command.specification;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonLocaleSpecConfiguration {
	public static final String ERROR_LOCALE_SPEC_BEAN = "errorMessageLocaleSpecBean";
	private static final String ERROR_LOCALE_SPEC_PROPERTIES_PREFIX = "skaro.pokedex.sdk.discord.embed-locale.error";

	public static final String WARNING_LOCALE_SPEC_BEAN = "warningMessageLocaleSpecBean";
	private static final String WARNING_LOCALE_SPEC_PROPERTIES_PREFIX = "skaro.pokedex.sdk.discord.embed-locale.warning";
	
	public static final String DISOCRD_PERMISSION_LOCALE_SPEC_BEAN = "discordPermissionLocaleSpecBean";
	private static final String DISOCRD_PERMISSION_LOCALE_SPEC_PROPERTIES_PREFIX = "skaro.pokedex.sdk.discord.embed-locale.discord-permissions";

	@Bean(ERROR_LOCALE_SPEC_BEAN)
	@ConfigurationProperties(ERROR_LOCALE_SPEC_PROPERTIES_PREFIX)
	@Valid
	public DiscordEmbedLocaleSpec errorMessageLocaleSpec() {
		return new DiscordEmbedLocaleSpec();
	}
	
	@Bean(WARNING_LOCALE_SPEC_BEAN)
	@ConfigurationProperties(WARNING_LOCALE_SPEC_PROPERTIES_PREFIX)
	@Valid
	public DiscordEmbedLocaleSpec warningMessageLocaleSpec() {
		return new DiscordEmbedLocaleSpec();
	}
	
	@Bean(DISOCRD_PERMISSION_LOCALE_SPEC_BEAN)
	@ConfigurationProperties(DISOCRD_PERMISSION_LOCALE_SPEC_PROPERTIES_PREFIX)
	@Valid
	public DiscordEmbedLocaleSpec warningMessageLocaleSpec(@Qualifier(WARNING_LOCALE_SPEC_BEAN) DiscordEmbedLocaleSpec warningLocaleSpec) {
		DiscordEmbedLocaleSpec result = new DiscordEmbedLocaleSpec();
		result.setColor(warningLocaleSpec.getColor());
		result.setThumbnail(warningLocaleSpec.getThumbnail());
		result.getEmbedSpecs().entrySet().stream()
			.forEach(entrySet -> {
				DiscordEmbedSpec spec = new DiscordEmbedSpec();
				spec.setTitle(entrySet.getValue().getTitle());
				result.getEmbedSpecs().put(entrySet.getKey(), spec);
			});
		return result;
	}
}
