package skaro.pokedex.sdk.worker.command;

import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.EmbedThumbnailData;
import discord4j.rest.util.Color;

public class MessageEmbedTemplates {

	public static final EmbedData ERROR_MESSAGE = EmbedData.builder()
			.color(Color.RED.getRGB())
			.title("Uh oh! An unexpected error occured.")
			.thumbnail(EmbedThumbnailData.builder()
					.height(512)
					.width(512)
					.url("https://cdn0.iconfinder.com/data/icons/shift-free/32/Error-512.png")
					.build())
			.build();
	
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
