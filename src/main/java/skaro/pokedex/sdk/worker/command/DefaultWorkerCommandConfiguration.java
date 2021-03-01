package skaro.pokedex.sdk.worker.command;

import javax.validation.Valid;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import reactor.core.scheduler.Scheduler;
import skaro.pokedex.sdk.messaging.MessageReceiver;
import skaro.pokedex.sdk.worker.messaging.WorkRequest;

@Configuration
public class DefaultWorkerCommandConfiguration {
	public static final String COMMAND_BEAN_POSTFIX = "Command";
	
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
