package skaro.pokedex.sdk.worker.command.registration;

import java.util.List;
import java.util.Optional;

import skaro.pokedex.sdk.worker.command.Command;

public interface CommandRegistrar {

	List<CommandRegistration> getCommandRegistrations();
	Optional<Command> getCommandByNameOrAlias(String nameOrAlias);
	
}
