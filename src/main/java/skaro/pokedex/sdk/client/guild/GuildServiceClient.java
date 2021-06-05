package skaro.pokedex.sdk.client.guild;

import reactor.core.publisher.Mono;

public interface GuildServiceClient {

	Mono<GuildSettings> getSettings(String id);
	Mono<GuildSettings> saveSettings(String id, GuildSettings settings);
	
}
