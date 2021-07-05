package skaro.pokedex.sdk.client.guild;

import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.netty.http.client.HttpClient;
import skaro.pokedex.sdk.client.CacheFacade;
import skaro.pokedex.sdk.client.ClientConfigurationProperties;

@Configuration
public class GuildServiceClientConfiguration {
	public static final String CONFIGURATION_PROPERTIES_PREFIX = "skaro.pokedex.client.guild-service";
	public static final String GUILD_SERVICE_PROPERTIES_BEAN = "guildServiceClientConfigurationProperties";
	public static final String GUILD_SERVICE_WEBCLIENT_BEAN = "guildServiceWebClientBean";
	
	@Bean(GUILD_SERVICE_PROPERTIES_BEAN)
	@ConfigurationProperties(CONFIGURATION_PROPERTIES_PREFIX)
	@Valid
	public ClientConfigurationProperties guildServiceClientConfigurationProperties() {
		return new ClientConfigurationProperties();
	}
	
	@Bean(GUILD_SERVICE_WEBCLIENT_BEAN)
	public WebClient webClient(HttpClient httpClient, @Qualifier(GUILD_SERVICE_PROPERTIES_BEAN) ClientConfigurationProperties configurationProperties) {
		return WebClient.builder()
				.clientConnector(new ReactorClientHttpConnector(httpClient))
				.baseUrl(configurationProperties.getBaseUri().toString())
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.build();
	}
	
	@Bean	
	public GuildServiceClient guildServiceClient(@Qualifier(GUILD_SERVICE_WEBCLIENT_BEAN) WebClient webClient, Optional<CacheFacade> cacheFacade) {
		return new CachingGuildServiceClient(webClient, cacheFacade);
	}
	
}
