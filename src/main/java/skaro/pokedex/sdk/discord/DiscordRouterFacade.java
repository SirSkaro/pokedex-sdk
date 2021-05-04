package skaro.pokedex.sdk.discord;

import java.util.List;

import discord4j.discordjson.json.MemberData;
import discord4j.discordjson.json.MessageCreateRequest;
import discord4j.discordjson.json.RoleData;
import discord4j.rest.http.client.ClientResponse;
import reactor.core.publisher.Mono;

public interface DiscordRouterFacade {

	Mono<ClientResponse> createMessage(MessageCreateRequest message, String channelId);
	Mono<MemberData> getMember(String guildId, String userId);
	Mono<List<RoleData>> getGuildRoles(String guildId);
	
}
