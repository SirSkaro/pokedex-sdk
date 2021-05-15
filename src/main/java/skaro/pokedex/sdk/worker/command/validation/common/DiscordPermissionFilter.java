package skaro.pokedex.sdk.worker.command.validation.common;

import java.util.List;

import discord4j.discordjson.json.MemberData;
import discord4j.discordjson.json.MessageCreateRequest;
import discord4j.discordjson.json.RoleData;
import discord4j.rest.http.client.ClientResponse;
import discord4j.rest.util.PermissionSet;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import skaro.pokedex.sdk.discord.DiscordRouterFacade;
import skaro.pokedex.sdk.messaging.dispatch.AnsweredWorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkStatus;
import skaro.pokedex.sdk.worker.command.MessageCreateRequestBuilder;
import skaro.pokedex.sdk.worker.command.validation.ValidationFilter;

public class DiscordPermissionFilter implements ValidationFilter {
	private PermissionSet requiredPermissions;
	private DiscordRouterFacade router;
	private MessageCreateRequestBuilder<DiscordPermissionMessageContent> requestBuilder;
	
	public DiscordPermissionFilter(PermissionSet requiredPermissions, DiscordRouterFacade router, MessageCreateRequestBuilder<DiscordPermissionMessageContent> requestBuilder) {
		this.requiredPermissions = requiredPermissions;
		this.router = router;
		this.requestBuilder = requestBuilder;
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
		return router.getMember(guildId, userId)
				.zipWith(router.getGuildRoles(guildId));
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
		
		AnsweredWorkRequest answer = new AnsweredWorkRequest();
		answer.setStatus(WorkStatus.BAD_REQUEST);
		answer.setWorkRequest(request);
		
		return sendInvalidRequestResponse(request)
				.thenReturn(answer);
	}
	
	private Mono<ClientResponse> sendInvalidRequestResponse(WorkRequest request) {
		return router.createMessage(createWarningMessage(request), request.getChannelId());
	}
	
	private MessageCreateRequest createWarningMessage(WorkRequest request) {
		DiscordPermissionMessageContent messageContent = new DiscordPermissionMessageContent();
		messageContent.setRequiredPermissions(requiredPermissions);
		messageContent.setWorkRequest(request);
		
		return requestBuilder.populateFrom(messageContent);
	}

}
