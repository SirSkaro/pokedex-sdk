package skaro.pokedex.sdk.discord;

import discord4j.discordjson.json.MessageCreateRequest;

public interface MessageBuilder<T extends MessageContent> {

	MessageCreateRequest populateFrom(T messageContent);
	
}
