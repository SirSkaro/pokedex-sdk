package skaro.pokedex.sdk.discord;

import discord4j.discordjson.json.MessageCreateRequest;
import discord4j.rest.http.client.ClientResponse;
import reactor.core.publisher.Mono;

public interface DiscordRouterFacade {

	Mono<ClientResponse> createMessage(MessageCreateRequest message, String channelId);
	
}
