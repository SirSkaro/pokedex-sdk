package skaro.pokedex.sdk.messaging.dispatch;

import java.io.Serializable;
import java.util.List;

import skaro.pokedex.sdk.resource.Language;

public class WorkRequest implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String commmand;
	private List<String> arguments;
	private String channelId;
	private String guildId;
	private String authorId;
	private Language language;
	
	public String getCommmand() {
		return commmand;
	}
	public void setCommmand(String commmand) {
		this.commmand = commmand;
	}
	public List<String> getArguments() {
		return arguments;
	}
	public void setArguments(List<String> arguments) {
		this.arguments = arguments;
	}
	public String getChannelId() {
		return channelId;
	}
	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}
	public String getGuildId() {
		return guildId;
	}
	public void setGuildId(String guildId) {
		this.guildId = guildId;
	}
	public String getAuthorId() {
		return authorId;
	}
	public void setAuthorId(String authorId) {
		this.authorId = authorId;
	}
	public Language getLanguage() {
		return language;
	}
	public void setLanguage(Language language) {
		this.language = language;
	}
	
}
