package skaro.pokedex.sdk.worker.command;

import javax.validation.Valid;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import reactor.core.scheduler.Scheduler;
import skaro.pokedex.sdk.messaging.MessageReceiver;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequest;
import skaro.pokedex.sdk.worker.command.manager.CommandManager;
import skaro.pokedex.sdk.worker.command.manager.ReportingCommandManager;
import skaro.pokedex.sdk.worker.command.registration.BeanCommandRegistrar;
import skaro.pokedex.sdk.worker.command.registration.CommandRegistrar;
import skaro.pokedex.sdk.worker.command.source.CommandSource;
import skaro.pokedex.sdk.worker.command.source.CommandSourceRunner;
import skaro.pokedex.sdk.worker.command.validation.ArgumentValidationChainAspectConfiguration;

@Configurable
@Import({
	ErrorRecoveryAspectConfiguration.class,
	ArgumentValidationChainAspectConfiguration.class
	})
public class DefaultWorkerCommandConfiguration {
	public static final String COMMAND_BEAN_POSTFIX = "Command";
	public static final int ERROR_RECOVERY_ASPECT_ORDER = 0;
	public static final int ARGUMENT_VALIDATION_ASPECT_ORDER = 1;
	
	@Bean
	@Valid
	@ConfigurationProperties(WorkerCommandConfigurationProperties.WORKER_PROPERTIES_PREFIX)
	public WorkerCommandConfigurationProperties workerConfigurationProperties() {
		return new WorkerCommandConfigurationProperties();
	}
	
	@Bean
	public CommandRegistrar commandRegistrar(WorkerCommandConfigurationProperties commandConfigurationProperties, BeanFactory beanFactory) {
		return new BeanCommandRegistrar(commandConfigurationProperties, beanFactory);
	}
	
	@Bean
	public CommandManager commandManager(CommandRegistrar registrar, Scheduler scheduler) {
		return new ReportingCommandManager(registrar, scheduler);
	}
	
	@Bean
	public CommandSource commandSrouce(CommandManager manager, MessageReceiver<WorkRequest> receiver, Scheduler scheduler) {
		return new CommandSourceRunner(manager, receiver, scheduler);
	}
	
}
