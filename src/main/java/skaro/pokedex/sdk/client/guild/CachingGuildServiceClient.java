package skaro.pokedex.sdk.client.guild;

import java.net.URI;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;

import reactor.core.publisher.Mono;
import skaro.pokedex.sdk.client.CacheFacade;

public class CachingGuildServiceClient implements GuildServiceClient {
	public static final String GUILD_SETTINGS_ENDPOINT = "/guild-settings";
	
	private WebClient webClient;
	private Optional<CacheFacade> cacheFacade;
	
	public CachingGuildServiceClient(WebClient webClient, Optional<CacheFacade> cacheFacade) {
		this.webClient = webClient;
		this.cacheFacade = cacheFacade;
	}

	@Override
	public Mono<GuildSettings> getSettings(String id) {
		return checkCache(id)
				.switchIfEmpty(Mono.defer(() -> fetchAndCacheGuildSettings(id)));
	}
	
	@Override
	public Mono<GuildSettings> saveSettings(String id, GuildSettings settings) {
		return putGuildSettings(id, settings)
				.flatMap(savedSettings -> cache(id, savedSettings));
	}
	
	private Mono<GuildSettings> checkCache(String id) {
		return Mono.justOrEmpty(cacheFacade)
				.flatMap(cache -> cache.get(GuildSettings.class, id));
	}
	
	private Mono<GuildSettings> fetchAndCacheGuildSettings(String id) {
		return fetchGuildSettings(id)
				.flatMap(guildSettings -> cache(id, guildSettings));
	}
	
	private Mono<GuildSettings> putGuildSettings(String id, GuildSettings settings) {
		return webClient.put()
				.uri(createUri(id))
				.accept(MediaType.APPLICATION_JSON)
				.bodyValue(settings)
				.retrieve()
				.bodyToMono(GuildSettings.class);
	}
	
	private Mono<GuildSettings> fetchGuildSettings(String id) {
		ResponseSpec responseSpec = webClient.get()
			.uri(createUri(id))
			.retrieve();
		
		return Mono.just(responseSpec)
				.flatMap(response -> response.bodyToMono(GuildSettings.class)
						.onErrorResume(WebClientResponseException.NotFound.class, notFound -> Mono.empty()));
	}
	
	private Mono<GuildSettings> cache(String id, GuildSettings guildSettings) {
		return Mono.justOrEmpty(cacheFacade)
				.map(cache -> cache.cache(id, guildSettings))
				.switchIfEmpty(Mono.just(guildSettings));
	}
	
	private Function<UriBuilder, URI> createUri(String settingsId) {
		return uriBuilder -> uriBuilder
				.path(GUILD_SETTINGS_ENDPOINT)
				.path("/{id}")
				.build(settingsId);
	}

}
