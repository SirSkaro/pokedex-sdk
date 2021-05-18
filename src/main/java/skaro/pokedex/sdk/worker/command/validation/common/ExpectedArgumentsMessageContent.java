package skaro.pokedex.sdk.worker.command.validation.common;

import javax.validation.constraints.NotNull;

import skaro.pokedex.sdk.client.Language;
import skaro.pokedex.sdk.discord.MessageContent;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequest;

public class ExpectedArgumentsMessageContent implements MessageContent {

	@NotNull
	private WorkRequest workRequest;
	
	public WorkRequest getWorkRequest() {
		return workRequest;
	}
	public void setWorkRequest(WorkRequest workRequest) {
		this.workRequest = workRequest;
	}
	@Override
	public Language getLanguage() {
		return workRequest.getLanguage();
	}
	
}
