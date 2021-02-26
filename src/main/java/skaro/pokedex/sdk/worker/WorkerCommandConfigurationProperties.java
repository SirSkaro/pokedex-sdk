package skaro.pokedex.sdk.worker;

import java.util.List;

import javax.validation.constraints.NotEmpty;

public class WorkerCommandConfigurationProperties {
	public static final String WORKER_PROPERTIES_PREFIX = "skaro.pokedex.worker";
	
	@NotEmpty
	private List<CommandRegistration> commands;

	public List<CommandRegistration> getCommands() {
		return commands;
	}

	public void setCommands(List<CommandRegistration> commands) {
		this.commands = commands;
	}
	
}
