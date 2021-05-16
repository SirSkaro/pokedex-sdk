package skaro.pokedex.sdk.discord;

import discord4j.rest.http.client.ClientResponse;
import reactor.core.publisher.Mono;

public interface DiscordMessageDirector<T extends MessageContent> {

	 Mono<ClientResponse> createDiscordMessage(T messageContent, String channelId);
	
}
