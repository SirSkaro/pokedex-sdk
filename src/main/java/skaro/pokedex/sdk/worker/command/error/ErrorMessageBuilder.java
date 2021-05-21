package skaro.pokedex.sdk.worker.command.error;

import java.util.List;

import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.EmbedFieldData;
import discord4j.discordjson.json.EmbedThumbnailData;
import discord4j.discordjson.json.MessageCreateRequest;
import skaro.pokedex.sdk.discord.MessageBuilder;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequest;
import skaro.pokedex.sdk.worker.command.specification.DiscordEmbedLocaleSpec;
import skaro.pokedex.sdk.worker.command.specification.DiscordEmbedSpec;

public class ErrorMessageBuilder implements MessageBuilder<ErrorMessageContent> {

	private DiscordEmbedLocaleSpec localeSpec;
	private int TECHNICAL_ERROR_FIELD_INDEX = 0;
	private int USER_INPUT_FIELD_INDEX = 1;
	private int SUPPORT_SERVER_FIELD_INDEX = 2;
	
	public ErrorMessageBuilder(DiscordEmbedLocaleSpec localeSpec) {
		this.localeSpec = localeSpec;
	}

	@Override
	public MessageCreateRequest populateFrom(ErrorMessageContent messageContent) {
		DiscordEmbedSpec embedSpec = localeSpec.getEmbedSpecs().get(messageContent.getLanguage());
		
		EmbedData embed = EmbedData.builder()
				.color(localeSpec.getColor())
				.title(embedSpec.getTitle())
				.description(embedSpec.getDescription())
				.fields(createEmbedFields(messageContent, embedSpec))
				.thumbnail(EmbedThumbnailData.builder()
						.url(localeSpec.getThumbnail().toString())
						.build())
				.build();
		
		return MessageCreateRequest.builder()
				.embed(embed)
				.build();
	}
	
	private List<EmbedFieldData> createEmbedFields(ErrorMessageContent messageContent, DiscordEmbedSpec embedSpec) {
		Throwable error = messageContent.getError();
		WorkRequest workRequest = messageContent.getWorkRequest();
		
		EmbedFieldData technicalErrorField = EmbedFieldData.builder()
				.inline(true)
				.name(embedSpec.getFields().get(TECHNICAL_ERROR_FIELD_INDEX).getName())
				.value(error.getMessage())
				.build();
		EmbedFieldData userInputField = EmbedFieldData.builder()
				.inline(true)
				.name(embedSpec.getFields().get(USER_INPUT_FIELD_INDEX).getName())
				.value(String.format("%s %s", workRequest.getCommmand(), workRequest.getArguments()))
				.build();
		EmbedFieldData supportServerLinkField = embedSpec.getFields().get(SUPPORT_SERVER_FIELD_INDEX).toEmbedFieldData();
		
		return List.of(technicalErrorField, userInputField, supportServerLinkField);
	}

}
