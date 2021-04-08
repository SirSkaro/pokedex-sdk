package skaro.pokedex.sdk.worker.command.validation.common;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import discord4j.discordjson.json.MemberData;
import discord4j.discordjson.json.RoleData;
import discord4j.rest.request.Router;
import discord4j.rest.route.Routes;
import discord4j.rest.util.PermissionSet;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import skaro.pokedex.sdk.messaging.dispatch.AnsweredWorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkStatus;
import skaro.pokedex.sdk.worker.command.validation.ValidationFilter;

public class DiscordPermissionFilter implements ValidationFilter {
	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private PermissionSet requiredPermissions;
	private Router router;
	
	public DiscordPermissionFilter(PermissionSet requiredPermissions, Router router) {
		this.requiredPermissions = requiredPermissions;
		this.router = router;
	}
	
	@Override
	public Mono<AnsweredWorkRequest> filter(WorkRequest request) {
		return getMemberPermissions(request.getGuildId(), request.getAuthorId())
			.flatMap(this::verifyUserHasRequiredPermissions);
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
	
	private Mono<AnsweredWorkRequest> verifyUserHasRequiredPermissions(PermissionSet userPermissions) {
		if(userPermissions.containsAll(requiredPermissions)) {
			LOG.info("You may change the prefix");
			return Mono.empty();
		}
		
		LOG.warn("You don't have permissions to change the prefix");
		AnsweredWorkRequest answer = new AnsweredWorkRequest();
		answer.setStatus(WorkStatus.BAD_REQUEST);
		return Mono.just(answer);
	}

}
