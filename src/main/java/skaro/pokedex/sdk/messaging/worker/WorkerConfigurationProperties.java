package skaro.pokedex.sdk.messaging.worker;

import javax.validation.constraints.NotEmpty;

public class WorkerConfigurationProperties {
	public static final String WORKER_PROPERTIES_PREFIX = "skaro.pokedex.worker";
	
	@NotEmpty
	private String queue;

	public String getQueue() {
		return queue;
	}
	public void setQueue(String queue) {
		this.queue = queue;
	}
	
}
