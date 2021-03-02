package skaro.pokedex.sdk.worker.command;

import org.springframework.beans.BeansException;

public class WorkerCommandConfigurationException extends BeansException {
	private static final long serialVersionUID = 1L;
	
	public WorkerCommandConfigurationException(String message) {
		super(message);
	}
	
	public WorkerCommandConfigurationException(String message, Throwable throwable) {
		super(message, throwable);
	}

}
