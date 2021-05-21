package skaro.pokedex.sdk.worker.command.validation.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import discord4j.discordjson.json.MemberData;
import discord4j.discordjson.json.RoleData;
import discord4j.rest.http.client.ClientResponse;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import skaro.pokedex.sdk.client.Language;
import skaro.pokedex.sdk.discord.DiscordMessageDirector;
import skaro.pokedex.sdk.discord.DiscordRouterFacade;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkStatus;

@ExtendWith(SpringExtension.class)
public class DiscordPermissionsFilterTest {

	@Mock
	private DiscordRouterFacade router;
	@Mock
	private DiscordMessageDirector<DiscordPermissionsMessageContent> messageDirector;
	
	private DiscordPermissionsFilter filter;
	
	@Test
	public void filterTest_userHasPermissions() {
		Permission requiredPermission1 = Permission.VIEW_GUILD_INSIGHTS;
		Permission requiredPermission2 = Permission.MANAGE_CHANNELS;
		WorkRequest workRequest = createWorkRequest();
		MemberData member = Mockito.mock(MemberData.class);
		RoleData memberRole1 = Mockito.mock(RoleData.class);
		String role1Id = UUID.randomUUID().toString();
		Mockito.when(memberRole1.id())
			.thenReturn(role1Id);
		RoleData memberRole2 = Mockito.mock(RoleData.class);
		String role2Id = UUID.randomUUID().toString();
		Mockito.when(memberRole2.id())
			.thenReturn(role2Id);
		RoleData role3 = Mockito.mock(RoleData.class);
		String role3Id = UUID.randomUUID().toString();
		Mockito.when(role3.id())
			.thenReturn(role3Id);
		
		Mockito.when(member.roles())
			.thenReturn(List.of(role1Id, role2Id));
		Mockito.when(memberRole1.permissions())
			.thenReturn(requiredPermission1.getValue());
		Mockito.when(memberRole2.permissions())
			.thenReturn(requiredPermission2.getValue());
		Mockito.when(router.getMember(workRequest.getGuildId(), workRequest.getAuthorId()))
			.thenReturn(Mono.just(member));
		Mockito.when(router.getGuildRoles(workRequest.getGuildId()))
			.thenReturn(Mono.just(List.of(memberRole1, memberRole2, role3)));
		
		PermissionSet requiredPermissions = PermissionSet.of(requiredPermission1, requiredPermission2);
		filter = new DiscordPermissionsFilter(requiredPermissions, router, messageDirector);
		
		StepVerifier.create(filter.filter(workRequest))
			.expectComplete()
			.verify();		
	}
	
	@Test
	public void filterTest_userDoesNotHavePermissions() throws URISyntaxException {
		WorkRequest workRequest = createWorkRequest();
		workRequest.setChannelId("channel id");
		MemberData member = Mockito.mock(MemberData.class);
		String roleId = UUID.randomUUID().toString();
		RoleData memberRole = Mockito.mock(RoleData.class);
		Mockito.when(memberRole.id())
			.thenReturn(roleId);
		
		Mockito.when(member.roles())
			.thenReturn(List.of(roleId));
		Mockito.when(memberRole.permissions())
			.thenReturn(Permission.CHANGE_NICKNAME.getValue());

		Mockito.when(router.getMember(workRequest.getGuildId(), workRequest.getAuthorId()))
			.thenReturn(Mono.just(member));
		Mockito.when(router.getGuildRoles(workRequest.getGuildId()))
			.thenReturn(Mono.just(List.of(memberRole)));
		Mockito.when(messageDirector.createDiscordMessage(any(DiscordPermissionsMessageContent.class), eq(workRequest.getChannelId())))
			.thenReturn(Mono.just(Mockito.mock(ClientResponse.class)));
		
		PermissionSet requiredPermissions = PermissionSet.of(Permission.DEAFEN_MEMBERS);
		filter = new DiscordPermissionsFilter(requiredPermissions, router, messageDirector);
		
		StepVerifier.create(filter.filter(workRequest))
			.assertNext(answer -> assertEquals(WorkStatus.BAD_REQUEST, answer.getStatus()))
			.expectComplete()
			.verify();	
	}
	
	private WorkRequest createWorkRequest() {
		WorkRequest workRequest = new WorkRequest();
		workRequest.setGuildId(UUID.randomUUID().toString());
		workRequest.setAuthorId(UUID.randomUUID().toString());
		workRequest.setChannelId(UUID.randomUUID().toString());
		workRequest.setLanguage(Language.ENGLISH);
		
		return workRequest;
	}
	
}
