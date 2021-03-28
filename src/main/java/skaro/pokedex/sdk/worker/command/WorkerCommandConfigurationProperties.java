package skaro.pokedex.sdk.worker.command;

import java.util.List;

import javax.validation.constraints.NotEmpty;

import skaro.pokedex.sdk.worker.command.registration.CommandRegistration;

public class WorkerCommandConfigurationProperties {
	public static final String WORKER_PROPERTIES_PREFIX = "skaro.pokedex.worker";
	
	@NotEmpty
	private List<CommandRegistration> commands;

	public List<CommandRegistration> getCommandRegistrations() {
		return commands;
	}

	public void setCommands(List<CommandRegistration> commands) {
		this.commands = commands;
	}
	
}
