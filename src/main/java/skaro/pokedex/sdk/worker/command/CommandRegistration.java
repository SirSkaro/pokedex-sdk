package skaro.pokedex.sdk.worker.command;

import java.util.List;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class CommandRegistration {

	@NotEmpty
	private String name;
	@NotNull
	private List<@NotEmpty String> aliases;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<String> getAliases() {
		return aliases;
	}
	public void setAliases(List<String> aliases) {
		this.aliases = aliases;
	}
	
}
