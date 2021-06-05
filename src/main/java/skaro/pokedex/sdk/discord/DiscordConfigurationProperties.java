package skaro.pokedex.sdk.discord;

import javax.validation.constraints.NotEmpty;

public class DiscordConfigurationProperties {
	public static final String DISCORD_PROPERTIES_PREFIX = "discord";
	
	@NotEmpty
	private String token;
	
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
}
