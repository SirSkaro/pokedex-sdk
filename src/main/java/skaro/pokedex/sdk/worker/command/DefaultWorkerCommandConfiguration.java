package skaro.pokedex.sdk.worker.command;

import javax.validation.Valid;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

import reactor.core.scheduler.Scheduler;
import skaro.pokedex.sdk.messaging.MessageReceiverHotStream;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequest;
import skaro.pokedex.sdk.worker.command.error.ErrorRecoveryAspectConfiguration;
import skaro.pokedex.sdk.worker.command.manager.CommandManager;
import skaro.pokedex.sdk.worker.command.manager.ReportingCommandManager;
import skaro.pokedex.sdk.worker.command.registration.BeanCommandRegistrar;
import skaro.pokedex.sdk.worker.command.registration.CommandRegistrar;
import skaro.pokedex.sdk.worker.command.source.CommandMessageReceiverSource;
import skaro.pokedex.sdk.worker.command.source.CommandSource;
import skaro.pokedex.sdk.worker.command.specification.CommonLocaleSpecConfiguration;
import skaro.pokedex.sdk.worker.command.validation.ArgumentValidationChainAspectConfiguration;

@Configurable
@Import({
	ErrorRecoveryAspectConfiguration.class,
	ArgumentValidationChainAspectConfiguration.class,
	CommonLocaleSpecConfiguration.class
})
@PropertySource("classpath:sdk.properties")
public class DefaultWorkerCommandConfiguration {
	public static final String COMMAND_BEAN_POSTFIX = "Command";
	public static final int ERROR_RECOVERY_ASPECT_ORDER = 0;
	public static final int ARGUMENT_VALIDATION_ASPECT_ORDER = 1;
	public static final int RATE_LIMIT_ASPECT_ORDER = 2;
	
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
	public CommandSource commandSrouce(CommandManager manager, MessageReceiverHotStream<WorkRequest> receiver, Scheduler scheduler) {
		return new CommandMessageReceiverSource(manager, receiver, scheduler);
	}
	
	@Bean
	public CommandLineRunner commandSourceRunner(CommandSource commandSource) {
		return (String[] args) -> commandSource.stream().subscribe();
	}
	
}
