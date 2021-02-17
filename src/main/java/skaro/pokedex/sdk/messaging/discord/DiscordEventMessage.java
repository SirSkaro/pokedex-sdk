package skaro.pokedex.sdk.messaging.discord;

import java.io.Serializable;

public interface DiscordEventMessage extends Serializable {

	String getChannelId();
	String getGuildId();
	String getAuthorId();
	
}
