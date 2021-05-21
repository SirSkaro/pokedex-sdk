package skaro.pokedex.sdk.worker.command.validation.common;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import discord4j.rest.http.client.ClientResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import skaro.pokedex.sdk.discord.DiscordMessageDirector;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequest;

@ExtendWith(SpringExtension.class)
public class ExpectedArgumentsFilterTest {

	private int expectedArgumentCount;
	@Mock
	private DiscordMessageDirector<ExpectedArgumentsMessageContent> messageDirector;
	private ExpectedArgumentsFilter filter;
	
	@BeforeEach
	public void setup() {
		expectedArgumentCount = 1;
		filter = new ExpectedArgumentsFilter(expectedArgumentCount, messageDirector);
	}
	
	@Test
	public void testFilter_ExpectedArgumentCount() {
		WorkRequest request = new WorkRequest();
		request.setArguments(List.of("foo"));
		
		StepVerifier.create(filter.filter(request))
			.expectComplete()
			.verify();
	}
	
	@Test
	public void testFilter_NotExpectedArgumentCount() {
		WorkRequest request = new WorkRequest();
		request.setArguments(List.of("foo", "bar"));
		request.setChannelId("channel id");
		ClientResponse clientResponse = Mockito.mock(ClientResponse.class);
		
		Mockito.when(messageDirector.createDiscordMessage(any(ExpectedArgumentsMessageContent.class), eq(request.getChannelId())))
			.thenReturn(Mono.just(clientResponse));
		
		StepVerifier.create(filter.filter(request))
			.assertNext(answer -> answer.getWorkRequest().equals(request))
			.expectComplete()
			.verify();
	}
	
}
