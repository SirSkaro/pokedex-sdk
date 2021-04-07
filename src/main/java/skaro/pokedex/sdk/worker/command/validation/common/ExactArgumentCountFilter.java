package skaro.pokedex.sdk.worker.command.validation.common;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Mono;
import skaro.pokedex.sdk.messaging.dispatch.AnsweredWorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkStatus;
import skaro.pokedex.sdk.worker.command.validation.CommandFilter;

public class ExactArgumentCountFilter implements CommandFilter {
	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private int expectedCount;
	private String invalidMessage;
	
	public ExactArgumentCountFilter(int expectedCount, String invalidMessage) {
		this.expectedCount = expectedCount;
		this.invalidMessage = invalidMessage;
	}

	@Override
	public Mono<AnsweredWorkRequest> filter(WorkRequest request) {
		if(expectedCount != request.getArguments().size()) {
			LOG.warn(invalidMessage);
			AnsweredWorkRequest answer = new AnsweredWorkRequest();
			answer.setStatus(WorkStatus.BAD_REQUEST);
			return Mono.just(answer);
		}
		
		return Mono.empty();
	}

}
