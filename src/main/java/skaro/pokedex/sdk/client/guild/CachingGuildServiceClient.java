package skaro.pokedex.sdk.client.guild;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;

import reactor.core.publisher.Mono;
import skaro.pokedex.sdk.client.CacheFacade;

public class CachingGuildServiceClient implements GuildServiceClient {
	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
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
	
	private Mono<GuildSettings> checkCache(String id) {
		return Mono.justOrEmpty(cacheFacade)
				.flatMap(cache -> cache.get(GuildSettings.class, id));
	}
	
	private Mono<GuildSettings> fetchAndCacheGuildSettings(String id) {
		return fetchGuildSettings(id)
				.flatMap(guildSettings -> cache(id, guildSettings));
	}
	
	private Mono<GuildSettings> fetchGuildSettings(String id) {
		ResponseSpec responseSpec = webClient.get()
			.uri(uriBuilder -> uriBuilder
					.path("/guild-settings")
					.path("/{id}")
					.build(id))
			.retrieve();
		
		return bodyToClass(GuildSettings.class, responseSpec);
	}
	
	private Mono<GuildSettings> cache(String id, GuildSettings guildSettings) {
		return Mono.justOrEmpty(cacheFacade)
				.map(cache -> cache.cache(id, guildSettings))
				.switchIfEmpty(Mono.just(guildSettings));
	}
	
	private <T> Mono<T> bodyToClass(Class<T> cls, ResponseSpec responseSpec) {
		return Mono.just(responseSpec)
				.flatMap(response -> response.bodyToMono(cls).onErrorResume(this::logConnectionError));
	}
	
	@SuppressWarnings("rawtypes")
	private Mono logConnectionError(Throwable error) {
		if(error.getCause() instanceof IOException) {
			LOG.error("Unable to connect to guild service", error);
		}
		return Mono.empty();
	}
	
}
