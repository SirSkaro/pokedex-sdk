package skaro.pokedex.sdk.worker.command.specification;

import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.EmbedThumbnailData;
import discord4j.rest.util.Color;

public class MessageEmbedTemplates {
	public static final EmbedData INVALID_REQUEST_MESSAGE = EmbedData.builder()
			.color(Color.YELLOW.getRGB())
			.title("Invalid request.")
			.thumbnail(EmbedThumbnailData.builder()
					.height(512)
					.width(512)
					.url("https://cdn3.iconfinder.com/data/icons/signs-symbols-5/126/slice333-512.png")
					.build())
			.build();
}
