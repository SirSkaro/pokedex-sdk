package skaro.pokedex.sdk.worker.command.error;

import skaro.pokedex.sdk.client.Language;
import skaro.pokedex.sdk.discord.MessageContent;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequest;

public class ErrorMessageContent implements MessageContent {
	private WorkRequest workRequest;
	private Throwable error;
	
	public WorkRequest getWorkRequest() {
		return workRequest;
	}
	public void setWorkRequest(WorkRequest workRequest) {
		this.workRequest = workRequest;
	}
	public Throwable getError() {
		return error;
	}
	public void setError(Throwable error) {
		this.error = error;
	}
	@Override
	public Language getLanguage() {
		return workRequest.getLanguage();
	}

}
