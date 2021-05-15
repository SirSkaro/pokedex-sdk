package skaro.pokedex.sdk.worker.command.validation.common;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Mono;
import skaro.pokedex.sdk.discord.DiscordRouterFacade;
import skaro.pokedex.sdk.messaging.dispatch.AnsweredWorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkStatus;
import skaro.pokedex.sdk.worker.command.specification.DiscordEmbedLocaleSpec;
import skaro.pokedex.sdk.worker.command.specification.DiscordEmbedSpec;
import skaro.pokedex.sdk.worker.command.validation.ValidationFilter;

public class SingleArgumentFilter implements ValidationFilter {
	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private DiscordRouterFacade router;
	private DiscordEmbedLocaleSpec localeSpec;
	
	public SingleArgumentFilter(DiscordRouterFacade router) {
		this.router = router;
	}

	@Override
	public Mono<AnsweredWorkRequest> filter(WorkRequest request) {
		if(request.getArguments().size() == 1) {
			return Mono.empty();
		}
		
		
		sendInvalidationMessage(request);
		LOG.warn(invalidMessage);
		AnsweredWorkRequest answer = new AnsweredWorkRequest();
		answer.setStatus(WorkStatus.BAD_REQUEST);
		return Mono.just(answer);
	}

	private void sendInvalidationMessage(WorkRequest request) {
		
	}

}
