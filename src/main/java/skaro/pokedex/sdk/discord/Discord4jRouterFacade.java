package skaro.pokedex.sdk.discord;

import discord4j.discordjson.json.MessageCreateRequest;
import discord4j.rest.http.client.ClientResponse;
import discord4j.rest.request.Router;
import discord4j.rest.route.Routes;
import reactor.core.publisher.Mono;

public class Discord4jRouterFacade implements DiscordRouterFacade {

	private Router router;
	
	public Discord4jRouterFacade(Router router) {
		this.router = router;
	}

	@Override
	public Mono<ClientResponse> createMessage(MessageCreateRequest message, String channelId) {
		return Routes.MESSAGE_CREATE.newRequest(channelId)
				.body(message)
				.exchange(router)
				.mono();
	}

}
