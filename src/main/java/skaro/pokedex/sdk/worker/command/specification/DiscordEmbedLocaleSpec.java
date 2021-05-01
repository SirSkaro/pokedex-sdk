package skaro.pokedex.sdk.worker.command.specification;

import java.net.URI;
import java.util.Map;

import javax.validation.constraints.NotNull;

import skaro.pokedex.sdk.client.Language;

public class DiscordEmbedLocaleSpec {

	private Integer color;
	private URI thumbnail;
	@NotNull
	private Map<Language, @NotNull DiscordEmbedSpec> embedSpecs;
	
	public Integer getColor() {
		return color;
	}
	public void setColor(Integer color) {
		this.color = color;
	}
	public URI getThumbnail() {
		return thumbnail;
	}
	public void setThumbnail(URI thumbnail) {
		this.thumbnail = thumbnail;
	}
	public Map<Language, DiscordEmbedSpec> getEmbedSpecs() {
		return embedSpecs;
	}
	public void setEmbedSpecs(Map<Language, DiscordEmbedSpec> embedSpecs) {
		this.embedSpecs = embedSpecs;
	}
	
}
