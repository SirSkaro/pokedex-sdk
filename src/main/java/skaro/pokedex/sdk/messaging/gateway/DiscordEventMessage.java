package skaro.pokedex.sdk.messaging.gateway;

import java.io.Serializable;

public interface DiscordEventMessage extends Serializable {

	String getChannelId();
	String getGuildId();
	String getAuthorId();
	
}
