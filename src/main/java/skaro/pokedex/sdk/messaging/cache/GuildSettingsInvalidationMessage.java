package skaro.pokedex.sdk.messaging.cache;

import java.io.Serializable;

import skaro.pokedex.sdk.client.guild.GuildSettings;

public class GuildSettingsInvalidationMessage implements Serializable, DiscordGuildCacheEvictionMessage {
	private static final long serialVersionUID = 1L;
	
	private String guildId;

	public String getGuildId() {
		return guildId;
	}
	public void setGuildId(String guildId) {
		this.guildId = guildId;
	}
	@Override
	public Class<?> getEntityClass() {
		return GuildSettings.class;
	}

}
