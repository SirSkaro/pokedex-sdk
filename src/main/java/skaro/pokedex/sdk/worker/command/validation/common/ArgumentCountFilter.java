package skaro.pokedex.sdk.worker.command.validation.common;

import reactor.core.publisher.Mono;
import skaro.pokedex.sdk.discord.DiscordRouterFacade;
import skaro.pokedex.sdk.discord.MessageBuilder;
import skaro.pokedex.sdk.messaging.dispatch.AnsweredWorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkStatus;
import skaro.pokedex.sdk.worker.command.validation.ValidationFilter;

public class ArgumentCountFilter implements ValidationFilter {
	private DiscordRouterFacade router;
	private MessageBuilder<InvalidArgumentCountMessageContent> messageBuilder;
	private int expectedArgumentCount;
	
	public ArgumentCountFilter(DiscordRouterFacade router) {
		this.router = router;
	}

	@Override
	public Mono<AnsweredWorkRequest> filter(WorkRequest request) {
		if(request.getArguments().size() == expectedArgumentCount) {
			return Mono.empty();
		}
		
		
		sendInvalidationMessage(request);
		AnsweredWorkRequest answer = new AnsweredWorkRequest();
		answer.setStatus(WorkStatus.BAD_REQUEST);
		answer.setWorkRequest(request);
		return Mono.just(answer);
	}

	private void sendInvalidationMessage(WorkRequest request) {
		
	}

}
