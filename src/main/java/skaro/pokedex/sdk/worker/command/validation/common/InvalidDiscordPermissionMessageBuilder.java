package skaro.pokedex.sdk.worker.command.validation.common;

import java.util.stream.Collectors;

import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.EmbedThumbnailData;
import discord4j.discordjson.json.MessageCreateRequest;
import skaro.pokedex.sdk.worker.command.MessageCreateRequestBuilder;
import skaro.pokedex.sdk.worker.command.specification.DiscordEmbedLocaleSpec;
import skaro.pokedex.sdk.worker.command.specification.DiscordEmbedSpec;

public class InvalidDiscordPermissionMessageBuilder implements MessageCreateRequestBuilder<DiscordPermissionMessageContent> {

	private DiscordEmbedLocaleSpec localeSpec;
	
	public InvalidDiscordPermissionMessageBuilder(DiscordEmbedLocaleSpec localeSpec) {
		this.localeSpec = localeSpec;
	}

	@Override
	public MessageCreateRequest populateFrom(DiscordPermissionMessageContent messageContent) {
		DiscordEmbedSpec embedSpec = localeSpec.getEmbedSpecs().get(messageContent.getLanguage());
				
		EmbedData embed = EmbedData.builder()
				.color(localeSpec.getColor())
				.title(embedSpec.getTitle())
				.description(formatDescription(messageContent, embedSpec))
				.thumbnail(EmbedThumbnailData.builder()
						.url(localeSpec.getThumbnail().toString())
						.build())
				.build();
		
		return MessageCreateRequest.builder()
				.embed(embed)
				.build();
	}
	
	private String formatDescription(DiscordPermissionMessageContent messageContent, DiscordEmbedSpec embedSpec) {
		String bulletedRequiredPermissions = messageContent.getRequiredPermissions().stream()
				.map(permission -> String.format("%s %s", ":small_blue_diamond:", permission.name()))
				.collect(Collectors.joining("\n"));
		
		return String.format("%s:\n%s", embedSpec.getDescription(), messageContent.getWorkRequest().getCommmand(), bulletedRequiredPermissions);
	}

}
