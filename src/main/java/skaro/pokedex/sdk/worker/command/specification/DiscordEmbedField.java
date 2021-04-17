package skaro.pokedex.sdk.worker.command.specification;

import javax.validation.constraints.NotEmpty;

public class DiscordEmbedField  {

	@NotEmpty
	private String name;
	@NotEmpty
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
	
}
