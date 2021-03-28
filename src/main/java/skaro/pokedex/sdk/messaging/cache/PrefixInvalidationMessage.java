package skaro.pokedex.sdk.messaging.cache;

import java.io.Serializable;

public class PrefixInvalidationMessage implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String guildId;

	public String getGuildId() {
		return guildId;
	}

	public void setGuildId(String guildId) {
		this.guildId = guildId;
	}

}
