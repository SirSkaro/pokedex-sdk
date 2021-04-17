package skaro.pokedex.sdk.worker.command.validation.common;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.MemberData;
import discord4j.discordjson.json.MessageCreateRequest;
import discord4j.discordjson.json.RoleData;
import discord4j.rest.request.Router;
import discord4j.rest.route.Routes;
import discord4j.rest.util.PermissionSet;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import skaro.pokedex.sdk.messaging.dispatch.AnsweredWorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkStatus;
import skaro.pokedex.sdk.worker.command.MessageEmbedTemplates;
import skaro.pokedex.sdk.worker.command.validation.ValidationFilter;

public class DiscordPermissionFilter implements ValidationFilter {
	private PermissionSet requiredPermissions;
	private Router router;
	
	public DiscordPermissionFilter(PermissionSet requiredPermissions, Router router) {
		this.requiredPermissions = requiredPermissions;
		this.router = router;
	}
	
	@Override
	public Mono<AnsweredWorkRequest> filter(WorkRequest request) {
		return getMemberPermissions(request.getGuildId(), request.getAuthorId())
			.flatMap(permissions -> verifyUserHasRequiredPermissions(permissions, request));
	}
	
	private Mono<PermissionSet> getMemberPermissions(String guildId, String userId) {
		return getMemberDataAndGuildRoles(guildId, userId)
			.map(memberAndGuildRoles -> calculateMemberPermissions(memberAndGuildRoles.getT1(), memberAndGuildRoles.getT2()));
	}
	
	private Mono<Tuple2<MemberData, List<RoleData>>> getMemberDataAndGuildRoles(String guildId, String userId) {
		return getMember(guildId, userId)
				.zipWith(getGuildRoles(guildId));
	}
	
	private Mono<MemberData> getMember(String guildId, String userId) {
		return Routes.GUILD_MEMBER_GET.newRequest(guildId, userId)
				.exchange(router)
				.bodyToMono(MemberData.class);
	}
	
    public Mono<List<RoleData>> getGuildRoles(String guildId) {
        return Routes.GUILD_ROLES_GET.newRequest(guildId)
                .exchange(router)
                .bodyToMono(RoleData[].class)
                .map(Arrays::asList);
    }
	
	private PermissionSet calculateMemberPermissions(MemberData member, List<RoleData> roles) {
		long permissionBitSet = roles.stream()
			.filter(role -> member.roles().contains(role.id()))
			.map(RoleData::permissions)
			.reduce(0L, (partialBitSet, currentBitSet) -> partialBitSet | currentBitSet);
		
		return PermissionSet.of(permissionBitSet);
	}
	
	private Mono<AnsweredWorkRequest> verifyUserHasRequiredPermissions(PermissionSet userPermissions, WorkRequest request) {
		if(userPermissions.containsAll(requiredPermissions)) {
			return Mono.empty();
		}
		
		return sendInvalidationMessage(request);
	}
	
	private Mono<AnsweredWorkRequest> sendInvalidationMessage(WorkRequest request) {
		AnsweredWorkRequest answer = new AnsweredWorkRequest();
		answer.setStatus(WorkStatus.BAD_REQUEST);
		answer.setWorkRequest(request);
		MessageCreateRequest errorResponse = createWarningMessage(request);
		
		return Routes.MESSAGE_CREATE.newRequest(request.getChannelId())
			.body(errorResponse)
			.exchange(router)
			.mono()
			.thenReturn(answer);
	}
	
	private MessageCreateRequest createWarningMessage(WorkRequest request) {
		String bulletedRequiredPermissions = requiredPermissions.stream()
				.map(permission -> String.format("%s %s", ":small_blue_diamond:", permission.name()))
				.collect(Collectors.joining("\n"));
				
		EmbedData embed = EmbedData.builder().from(MessageEmbedTemplates.INVALID_REQUEST_MESSAGE)
				.description(String.format("You need to following permissions to use the **%s** command:\n%s", request.getCommmand(), bulletedRequiredPermissions))
				.build();
		
		return MessageCreateRequest.builder()
				.embed(embed)
				.build();
	}

}
