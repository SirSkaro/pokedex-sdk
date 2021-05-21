package skaro.pokedex.sdk.worker.command.validation.common;

import java.util.List;
import java.util.stream.Collectors;

import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.EmbedFieldData;
import discord4j.discordjson.json.EmbedThumbnailData;
import discord4j.discordjson.json.MessageCreateRequest;
import skaro.pokedex.sdk.discord.MessageBuilder;
import skaro.pokedex.sdk.worker.command.specification.DiscordEmbedField;
import skaro.pokedex.sdk.worker.command.specification.DiscordEmbedLocaleSpec;
import skaro.pokedex.sdk.worker.command.specification.DiscordEmbedSpec;

public class ExpectedArgumentsMessageBuilder implements MessageBuilder<ExpectedArgumentsMessageContent> {
	
	private DiscordEmbedLocaleSpec localeSpec;
	
	public ExpectedArgumentsMessageBuilder(DiscordEmbedLocaleSpec localeSpec) {
		this.localeSpec = localeSpec;
	}

	@Override
	public MessageCreateRequest populateFrom(ExpectedArgumentsMessageContent messageContent) {
		DiscordEmbedSpec embedSpec = localeSpec.getEmbedSpecs().get(messageContent.getLanguage());
		
		EmbedData embed = EmbedData.builder()
				.color(localeSpec.getColor())
				.title(embedSpec.getTitle())
				.description(formatDescription(messageContent, embedSpec))
				.addAllFields(formatFields(embedSpec))
				.thumbnail(EmbedThumbnailData.builder()
						.url(localeSpec.getThumbnail().toString())
						.build())
				.build();
		
		return MessageCreateRequest.builder()
				.embed(embed)
				.build();
	}

	private String formatDescription(ExpectedArgumentsMessageContent messageContent, DiscordEmbedSpec embedSpec) {
		return String.format(embedSpec.getDescription(), messageContent.getWorkRequest().getCommmand());
	}
	
	private List<EmbedFieldData> formatFields(DiscordEmbedSpec embedSpec) {
		return embedSpec.getFields().stream()
			.map(DiscordEmbedField::toEmbedFieldData)
			.collect(Collectors.toList());
	}

}
