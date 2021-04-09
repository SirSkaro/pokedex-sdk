package skaro.pokedex.sdk.worker.command;

import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.EmbedThumbnailData;
import discord4j.rest.util.Color;

public class MessageEmbedTemplates {

	public static EmbedData ERROR_MESSAGE = EmbedData.builder()
			.color(Color.RED.getRGB())
			.title("Uh oh! An unexpected error occured.")
			.thumbnail(EmbedThumbnailData.builder()
					.height(512)
					.width(512)
					.url("https://cdn0.iconfinder.com/data/icons/shift-free/32/Error-512.png")
					.build())
			.build();
	
}
