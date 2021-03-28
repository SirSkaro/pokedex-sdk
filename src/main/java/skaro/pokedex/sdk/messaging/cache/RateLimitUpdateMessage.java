package skaro.pokedex.sdk.messaging.cache;

import java.io.Serializable;

public class RateLimitUpdateMessage implements Serializable {
	private static final long serialVersionUID = 1L;

	private String guildId;
	private String channelId;
	private String userId;
	
	public String getGuildId() {
		return guildId;
	}
	public void setGuildId(String guildId) {
		this.guildId = guildId;
	}
	public String getChannelId() {
		return channelId;
	}
	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
}
