package skaro.pokedex.sdk.discord;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import discord4j.discordjson.json.MessageCreateRequest;
import discord4j.rest.http.client.ClientResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
public class MessageCreateRequestDirectorTest {

	@Mock
	private DiscordRouterFacade router;
	@Mock
	private MessageBuilder<MessageContent> builder;
	
	private MessageCreateRequestDirector<MessageContent> director;
	
	@BeforeEach
	private void setup() {
		director = new MessageCreateRequestDirector<MessageContent>(router, builder);
	}
	
	@Test
	private void testCreateDiscordMessage() {
		MessageContent content = Mockito.mock(MessageContent.class);
		String channelId = UUID.randomUUID().toString();
		MessageCreateRequest messageCreateRequest = Mockito.mock(MessageCreateRequest.class);
		ClientResponse clientResponse = Mockito.mock(ClientResponse.class);
		Mockito.when(builder.populateFrom(content))
			.thenReturn(messageCreateRequest);
		Mockito.when(router.createMessage(messageCreateRequest, channelId))
			.thenReturn(Mono.just(clientResponse));
		
		StepVerifier.create(director.createDiscordMessage(content, channelId))
			.expectNext(clientResponse)
			.expectComplete()
			.verify();
	}
	
}
