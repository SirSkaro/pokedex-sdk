package skaro.pokedex.sdk.worker.command.ratelimit;

import org.apache.commons.lang3.StringUtils;

import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.EmbedThumbnailData;
import discord4j.discordjson.json.MessageCreateRequest;
import skaro.pokedex.sdk.discord.MessageBuilder;
import skaro.pokedex.sdk.worker.command.specification.DiscordEmbedLocaleSpec;
import skaro.pokedex.sdk.worker.command.specification.DiscordEmbedSpec;

public class RateLimitMessageBuilder implements MessageBuilder<RateLimitMessageContent> {

	private DiscordEmbedLocaleSpec localeSpec;
	private String[] tokens;
	
	public RateLimitMessageBuilder(DiscordEmbedLocaleSpec localeSpec) {
		this.localeSpec = localeSpec;
		this.tokens = new String[]{"{command}", "{requests}", "{seconds}", "{time-left}"};
	}
	
	@Override
	public MessageCreateRequest populateFrom(RateLimitMessageContent messageContent) {
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

	private String formatDescription(RateLimitMessageContent messageContent, DiscordEmbedSpec embedSpec) {
		RateLimit rateLimit = messageContent.getRateLimit();
		String[] tokenValues = {
				messageContent.getCommand(), 
				Integer.toString(rateLimit.requests()), 
				Integer.toString(rateLimit.seconds()),
				Long.toString(messageContent.getTimeLeftInSeconds())
		};

		return StringUtils.replaceEach(embedSpec.getDescription(), tokens, tokenValues);
	}

}
