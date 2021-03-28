package skaro.pokedex.sdk.worker.command;

import static org.mockito.ArgumentMatchers.eq;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import skaro.pokedex.sdk.worker.command.registration.BeanCommandRegistrar;
import skaro.pokedex.sdk.worker.command.registration.CommandRegistration;

@ExtendWith(SpringExtension.class)
public class BeanCommandRegistrarTest {

	@Mock
	private WorkerCommandConfigurationProperties commandConfigurationProperties;
	@Mock
	private BeanFactory beanFactory;
	
	private BeanCommandRegistrar registrar;
	
	@Test
	public void testCreateRegistrarSuccessfulRegistration() {
		String commandName = "Foo";
		List<String> aliases = List.of("Fuu", "Phoo");
		String expectedCommandBeanName = commandName + DefaultWorkerCommandConfiguration.COMMAND_BEAN_POSTFIX;
		
		Command mockCommandBean = Mockito.mock(Command.class);
		CommandRegistration commandRegistrationWithAliases = new CommandRegistration();
		commandRegistrationWithAliases.setName(commandName);
		commandRegistrationWithAliases.setAliases(aliases);

		Mockito.when(commandConfigurationProperties.getCommandRegistrations())
			.thenReturn(List.of(commandRegistrationWithAliases));
		Mockito.when(beanFactory.getBean(expectedCommandBeanName, Command.class))
			.thenReturn(mockCommandBean);
		
		registrar = new BeanCommandRegistrar(commandConfigurationProperties, beanFactory);
		
		Stream.concat(Stream.of(commandName), aliases.stream())
			.map(registrar::getCommandByNameOrAlias)
			.map(Optional::isPresent)
			.forEach(Assertions::assertTrue);
	}
	
	@Test
	public void testCreateRegistrarMissingCommandBean() {
		String commandName = "bar";
		String expectedCommandBeanName = commandName + DefaultWorkerCommandConfiguration.COMMAND_BEAN_POSTFIX;
		CommandRegistration commandRegistration = new CommandRegistration();
		commandRegistration.setName(commandName);

		Mockito.when(commandConfigurationProperties.getCommandRegistrations())
			.thenReturn(List.of(commandRegistration));
		Mockito.when(beanFactory.getBean(eq(expectedCommandBeanName), eq(Command.class)))
			.thenThrow(NoSuchBeanDefinitionException.class);
		
		Assertions.assertThrows(WorkerCommandConfigurationException.class, () -> new BeanCommandRegistrar(commandConfigurationProperties, beanFactory));
	}
	
	@Test
	public void testCreateRegistrarDuplicateAlias() {
		String commandName = "Foo";
		List<String> aliases = List.of("Foo");
		String expectedCommandBeanName = commandName + DefaultWorkerCommandConfiguration.COMMAND_BEAN_POSTFIX;
		
		Command mockCommandBean = Mockito.mock(Command.class);
		CommandRegistration commandRegistrationWithAliases = new CommandRegistration();
		commandRegistrationWithAliases.setName(commandName);
		commandRegistrationWithAliases.setAliases(aliases);

		Mockito.when(commandConfigurationProperties.getCommandRegistrations())
			.thenReturn(List.of(commandRegistrationWithAliases));
		Mockito.when(beanFactory.getBean(expectedCommandBeanName, Command.class))
			.thenReturn(mockCommandBean);
		
		Assertions.assertThrows(WorkerCommandConfigurationException.class, () -> new BeanCommandRegistrar(commandConfigurationProperties, beanFactory));
	}
	
}
