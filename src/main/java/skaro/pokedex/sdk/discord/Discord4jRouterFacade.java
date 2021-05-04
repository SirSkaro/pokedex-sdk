package skaro.pokedex.sdk.discord;

import java.util.Arrays;
import java.util.List;

import discord4j.discordjson.json.MemberData;
import discord4j.discordjson.json.MessageCreateRequest;
import discord4j.discordjson.json.RoleData;
import discord4j.rest.http.client.ClientResponse;
import discord4j.rest.request.Router;
import discord4j.rest.route.Routes;
import reactor.core.publisher.Mono;

public class Discord4jRouterFacade implements DiscordRouterFacade {

	private Router router;
	
	public Discord4jRouterFacade(Router router) {
		this.router = router;
	}

	@Override
	public Mono<ClientResponse> createMessage(MessageCreateRequest message, String channelId) {
		return Routes.MESSAGE_CREATE.newRequest(channelId)
				.body(message)
				.exchange(router)
				.mono();
	}

	@Override
	public Mono<MemberData> getMember(String guildId, String userId) {
		return Routes.GUILD_MEMBER_GET.newRequest(guildId, userId)
				.exchange(router)
				.bodyToMono(MemberData.class);
	}

	@Override
	public Mono<List<RoleData>> getGuildRoles(String guildId) {
		return Routes.GUILD_ROLES_GET.newRequest(guildId)
                .exchange(router)
                .bodyToMono(RoleData[].class)
                .map(Arrays::asList);
	}

}
