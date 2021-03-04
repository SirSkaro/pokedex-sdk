package skaro.pokedex.sdk.worker.command;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

public class BeanCommandRegistrar implements CommandRegistrar {
	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private WorkerCommandConfigurationProperties commandConfigurationProperties;
	private Map<String, Command> commands;
	
	public BeanCommandRegistrar(WorkerCommandConfigurationProperties commandConfigurationProperties, BeanFactory beanFactory) {
		Map<String, Command> commandsMappedByNameAndAliases = new HashMap<>();
		
		commandConfigurationProperties.getCommandRegistrations()
		.forEach(commandRegistration -> {
			Command command = findByCommandName(beanFactory, commandRegistration.getName());
			mapNameAndAliases(commandsMappedByNameAndAliases, commandRegistration, command);
		});
		
		this.commandConfigurationProperties = commandConfigurationProperties;
		this.commands = Collections.unmodifiableMap(commandsMappedByNameAndAliases);
	}
	
	@Override
	public List<CommandRegistration> getCommandRegistrations() {
		return commandConfigurationProperties.getCommandRegistrations();
	}

	@Override
	public Optional<Command> getCommandByNameOrAlias(String nameOrAlias) {
		return Optional.ofNullable(commands.get(nameOrAlias));
	}
	
	private Command findByCommandName(BeanFactory beanFactory, String name) {
		String expectedCommandBeanName = name + DefaultWorkerCommandConfiguration.COMMAND_BEAN_POSTFIX;
		try {
			return beanFactory.getBean(expectedCommandBeanName, Command.class);
		} catch(NoSuchBeanDefinitionException | BeanNotOfRequiredTypeException e) {
			String errorMessage = new StringBuilder(String.format("Tried to register the command %s, but there was no bean '%s' of type %s", name, expectedCommandBeanName, Command.class.getName()))
					.append(" Have you registered the command as a bean using the expected naming convention?")
					.toString();
			throw new WorkerCommandConfigurationException(errorMessage, e);
		}
	}
	
	private void mapNameAndAliases(Map<String, Command> commands, CommandRegistration registration, Command command) {
		map(commands, registration.getName(), command);
		registration.getAliases().forEach(alias -> map(commands, alias, command));
		LOG.info("Matched command registration '{}' with bean {}", registration.getName(), command.getClass().getName());
	}
	
	private void map(Map<String, Command> commands, String key, Command value) {
		if(commands.containsKey(key)) {
			String errorMessage = String.format("Name or alias '%s' is already registered to %s", key, commands.get(key).getClass().getName());
			throw new WorkerCommandConfigurationException(errorMessage);
		}
		
		commands.put(key, value);
	}

}
