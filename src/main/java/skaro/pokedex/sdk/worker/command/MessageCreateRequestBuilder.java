package skaro.pokedex.sdk.worker.command;

import discord4j.discordjson.json.MessageCreateRequest;

public interface MessageCreateRequestBuilder<T extends MessageContent> {

	MessageCreateRequest populateFrom(T messageContent);
	
}
