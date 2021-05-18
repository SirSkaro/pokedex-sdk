package skaro.pokedex.sdk.worker.command.validation.common;

import discord4j.rest.http.client.ClientResponse;
import reactor.core.publisher.Mono;
import skaro.pokedex.sdk.discord.DiscordMessageDirector;
import skaro.pokedex.sdk.messaging.dispatch.AnsweredWorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkStatus;
import skaro.pokedex.sdk.worker.command.validation.ValidationFilter;

public class ExpectedArgumentsFilter implements ValidationFilter {
	private int expectedArgumentCount;
	private DiscordMessageDirector<ExpectedArgumentsMessageContent> messageDirector;
	
	public ExpectedArgumentsFilter(int expectedArgumentCount, DiscordMessageDirector<ExpectedArgumentsMessageContent> messageDirector) {
		this.expectedArgumentCount = expectedArgumentCount;
		this.messageDirector = messageDirector;
	}

	@Override
	public Mono<AnsweredWorkRequest> filter(WorkRequest request) {
		if(request.getArguments().size() == expectedArgumentCount) {
			return Mono.empty();
		}
		
		return sendInvalidRequestResponse(request)
				.thenReturn(createAnswer(request));
	}

	private Mono<ClientResponse> sendInvalidRequestResponse(WorkRequest request) {
		ExpectedArgumentsMessageContent messageContent = new ExpectedArgumentsMessageContent();
		messageContent.setWorkRequest(request);
		
		return messageDirector.createDiscordMessage(messageContent, request.getChannelId());
	}
	
	private AnsweredWorkRequest createAnswer(WorkRequest request) {
		AnsweredWorkRequest answer = new AnsweredWorkRequest();
		answer.setStatus(WorkStatus.BAD_REQUEST);
		answer.setWorkRequest(request);
		
		return answer;
	}

}
