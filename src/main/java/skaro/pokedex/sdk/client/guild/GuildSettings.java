package skaro.pokedex.sdk.client.guild;

import skaro.pokedex.sdk.client.Language;

public class GuildSettings {

	private String prefix;
	private Language language;
	
	public String getPrefix() {
		return prefix;
	}
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	public Language getLanguage() {
		return language;
	}
	public void setLanguage(Language language) {
		this.language = language;
	}
	
}
