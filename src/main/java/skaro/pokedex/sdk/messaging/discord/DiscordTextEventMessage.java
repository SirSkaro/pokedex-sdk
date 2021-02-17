package skaro.pokedex.sdk.messaging.discord;

public class DiscordTextEventMessage implements DiscordEventMessage {
	private static final long serialVersionUID = 1L;

	private String channelId;
	private String guildId;
	private String authorId;
	private String content;
	
	@Override
	public String getChannelId() {
		return channelId;
	}
	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}
	@Override
	public String getGuildId() {
		return guildId;
	}
	public void setGuildId(String guildId) {
		this.guildId = guildId;
	}
	@Override
	public String getAuthorId() {
		return authorId;
	}
	public void setAuthorId(String authorId) {
		this.authorId = authorId;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
}
