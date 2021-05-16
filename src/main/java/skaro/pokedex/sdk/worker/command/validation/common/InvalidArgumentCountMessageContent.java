package skaro.pokedex.sdk.worker.command.validation.common;

import skaro.pokedex.sdk.client.Language;
import skaro.pokedex.sdk.discord.MessageContent;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequest;

public class InvalidArgumentCountMessageContent implements MessageContent {

	private WorkRequest workRequest;
	private int expectedArgumentCount;
	
	public WorkRequest getWorkRequest() {
		return workRequest;
	}
	public void setWorkRequest(WorkRequest workRequest) {
		this.workRequest = workRequest;
	}
	public int getExpectedArgumentCount() {
		return expectedArgumentCount;
	}
	public void setExpectedArgumentCount(int expectedArgumentCount) {
		this.expectedArgumentCount = expectedArgumentCount;
	}

	@Override
	public Language getLanguage() {
		return workRequest.getLanguage();
	}

}
