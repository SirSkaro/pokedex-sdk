package skaro.pokedex.sdk.worker.command.error;

import static skaro.pokedex.sdk.worker.command.specification.CommonLocaleSpecConfiguration.ERROR_LOCALE_SPEC_BEAN;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import skaro.pokedex.sdk.discord.DiscordMessageDirector;
import skaro.pokedex.sdk.discord.DiscordRouterFacade;
import skaro.pokedex.sdk.discord.MessageCreateRequestDirector;
import skaro.pokedex.sdk.worker.command.specification.DiscordEmbedLocaleSpec;

@Configuration
public class ErrorMessageDirectorConfiguration {

	@Bean
	public DiscordMessageDirector<ErrorMessageContent> errorMessageDirector(@Qualifier(ERROR_LOCALE_SPEC_BEAN) DiscordEmbedLocaleSpec errorMessageLocaleSpec, DiscordRouterFacade router) {
		ErrorMessageBuilder builder = new ErrorMessageBuilder(errorMessageLocaleSpec);
		return new MessageCreateRequestDirector<ErrorMessageContent>(router, builder);
	}
	
}
