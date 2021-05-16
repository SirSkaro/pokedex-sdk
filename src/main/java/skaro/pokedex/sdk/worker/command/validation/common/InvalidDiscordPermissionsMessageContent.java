package skaro.pokedex.sdk.worker.command.validation.common;

import discord4j.rest.util.PermissionSet;
import skaro.pokedex.sdk.client.Language;
import skaro.pokedex.sdk.discord.MessageContent;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequest;

public class InvalidDiscordPermissionsMessageContent implements MessageContent {

	private PermissionSet requiredPermissions;
	private WorkRequest workRequest;
	
	public PermissionSet getRequiredPermissions() {
		return requiredPermissions;
	}
	public void setRequiredPermissions(PermissionSet requiredPermissions) {
		this.requiredPermissions = requiredPermissions;
	}
	public WorkRequest getWorkRequest() {
		return workRequest;
	}
	public void setWorkRequest(WorkRequest workRequest) {
		this.workRequest = workRequest;
	}
	@Override
	public Language getLanguage() {
		return workRequest.getLanguage();
	}
	
}
