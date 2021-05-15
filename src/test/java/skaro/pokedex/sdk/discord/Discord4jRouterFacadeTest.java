package skaro.pokedex.sdk.discord;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import discord4j.discordjson.json.MemberData;
import discord4j.discordjson.json.MessageCreateRequest;
import discord4j.discordjson.json.RoleData;
import discord4j.rest.http.client.ClientResponse;
import discord4j.rest.request.DiscordWebRequest;
import discord4j.rest.request.DiscordWebResponse;
import discord4j.rest.request.Router;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
public class Discord4jRouterFacadeTest {

	@Mock
	private Router router;
	private Discord4jRouterFacade facade;
	
	@BeforeEach
	public void setup() {
		facade = new Discord4jRouterFacade(router);
	}
	
	@Test
	public void testCreateMessage() {
		String channelId = UUID.randomUUID().toString();
		MessageCreateRequest request = Mockito.mock(MessageCreateRequest.class);
		DiscordWebResponse discordResponse = Mockito.mock(DiscordWebResponse.class);
		ClientResponse clientResponse = Mockito.mock(ClientResponse.class);
	
		Mockito.when(router.exchange(ArgumentMatchers.any(DiscordWebRequest.class)))
			.thenReturn(discordResponse);
		Mockito.when(discordResponse.mono())
			.thenReturn(Mono.just(clientResponse));
		
		StepVerifier.create(facade.createMessage(request, channelId))
			.expectNext(clientResponse)
			.expectComplete()
			.verify();
	}
	
	@Test
	public void testGetMember() {
		String guildId = UUID.randomUUID().toString();
		String userId = UUID.randomUUID().toString();
		DiscordWebResponse discordResponse = Mockito.mock(DiscordWebResponse.class);
		MemberData member = Mockito.mock(MemberData.class);
		
		Mockito.when(router.exchange(ArgumentMatchers.any(DiscordWebRequest.class)))
			.thenReturn(discordResponse);
		Mockito.when(discordResponse.bodyToMono(MemberData.class))
			.thenReturn(Mono.just(member));
		
		StepVerifier.create(facade.getMember(guildId, userId))
			.expectNext(member)
			.expectComplete()
			.verify();
	}
	
	@Test
	public void testGetGuildRoles() {
		String guildId = UUID.randomUUID().toString();
		DiscordWebResponse discordResponse = Mockito.mock(DiscordWebResponse.class);
		RoleData role1 = Mockito.mock(RoleData.class);
		RoleData role2 = Mockito.mock(RoleData.class);
		
		Mockito.when(router.exchange(ArgumentMatchers.any(DiscordWebRequest.class)))
			.thenReturn(discordResponse);
		Mockito.when(discordResponse.bodyToMono(RoleData[].class))
			.thenReturn(Mono.just(new RoleData[] {role1, role2}));
		
		StepVerifier.create(facade.getGuildRoles(guildId))
			.assertNext(roles -> roles.containsAll(List.of(role1, role2)))
			.expectComplete()
			.verify();
	}
	
}
