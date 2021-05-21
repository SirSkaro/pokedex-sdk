package skaro.pokedex.sdk.worker.command.specification;

import javax.validation.constraints.NotEmpty;

import discord4j.discordjson.json.EmbedFieldData;

public class DiscordEmbedField  {

	@NotEmpty
	private String name;
	private String value;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	public EmbedFieldData toEmbedFieldData() {
		return EmbedFieldData.builder()
				.name(name)
				.value(value)
				.build();
	}
	
}
