package skaro.pokedex.sdk.worker.command.validation.common;

import java.util.stream.Collectors;

import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.EmbedThumbnailData;
import discord4j.discordjson.json.MessageCreateRequest;
import skaro.pokedex.sdk.discord.MessageBuilder;
import skaro.pokedex.sdk.worker.command.specification.DiscordEmbedLocaleSpec;
import skaro.pokedex.sdk.worker.command.specification.DiscordEmbedSpec;

public class InvalidDiscordPermissionsMessageBuilder implements MessageBuilder<InvalidDiscordPermissionsMessageContent> {

	private DiscordEmbedLocaleSpec localeSpec;
	
	public InvalidDiscordPermissionsMessageBuilder(DiscordEmbedLocaleSpec localeSpec) {
		this.localeSpec = localeSpec;
	}

	@Override
	public MessageCreateRequest populateFrom(InvalidDiscordPermissionsMessageContent messageContent) {
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
	
	private String formatDescription(InvalidDiscordPermissionsMessageContent messageContent, DiscordEmbedSpec embedSpec) {
		String bulletedRequiredPermissions = messageContent.getRequiredPermissions().stream()
				.map(permission -> String.format("%s %s", ":small_blue_diamond:", permission.name()))
				.collect(Collectors.joining("\n"));
		String description = String.format(embedSpec.getDescription(), messageContent.getWorkRequest().getCommmand());
		
		return String.format("%s:\n%s", description, bulletedRequiredPermissions);
	}

}
