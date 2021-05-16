package skaro.pokedex.sdk.discord;

import discord4j.discordjson.json.MessageCreateRequest;
import discord4j.rest.http.client.ClientResponse;
import reactor.core.publisher.Mono;

public class MessageCreateRequestDirector<T extends MessageContent> implements DiscordMessageDirector<T> {

	private DiscordRouterFacade router;
	private MessageBuilder<T> builder;
	
	public MessageCreateRequestDirector(DiscordRouterFacade router, MessageBuilder<T> builder) {
		this.router = router;
		this.builder = builder;
	}

	public Mono<ClientResponse> createDiscordMessage(T messageContent, String channelId) {
		MessageCreateRequest createRequest = builder.populateFrom(messageContent);
		return router.createMessage(createRequest, channelId);
	}

}
