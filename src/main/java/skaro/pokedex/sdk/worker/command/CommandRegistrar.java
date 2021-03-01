package skaro.pokedex.sdk.worker.command;

import java.util.List;
import java.util.Optional;

public interface CommandRegistrar {

	List<CommandRegistration> getCommandRegistrations();
	Optional<Command> getCommandByNameOrAlias(String nameOrAlias);
	
}
