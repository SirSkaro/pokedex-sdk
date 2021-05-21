package skaro.pokedex.sdk.worker.command.validation.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.MessageCreateRequest;
import discord4j.rest.util.Color;
import skaro.pokedex.sdk.client.Language;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequest;
import skaro.pokedex.sdk.worker.command.specification.DiscordEmbedField;
import skaro.pokedex.sdk.worker.command.specification.DiscordEmbedLocaleSpec;
import skaro.pokedex.sdk.worker.command.specification.DiscordEmbedSpec;

@ExtendWith(SpringExtension.class)
public class ExpectedArgumentsMessageBuilderTest {

	private DiscordEmbedLocaleSpec localeSpec;
	private ExpectedArgumentsMessageBuilder builder;
	
	@BeforeEach
	public void setup() {
		localeSpec = createLocaleSpec();
		builder = new ExpectedArgumentsMessageBuilder(localeSpec);
	}
	
	@Test
	public void testPopulateFrom() {
		WorkRequest workRequest = new WorkRequest();
		workRequest.setCommmand("command");
		workRequest.setLanguage(Language.ENGLISH);
		ExpectedArgumentsMessageContent content = new ExpectedArgumentsMessageContent();
		content.setWorkRequest(workRequest);
		
		MessageCreateRequest result = builder.populateFrom(content);
		EmbedData resultEmbed = result.embed().get();
		DiscordEmbedSpec embedSpec = localeSpec.getEmbedSpecs().get(workRequest.getLanguage());
		
		assertEquals(localeSpec.getColor(), resultEmbed.color().get());
		assertEquals(localeSpec.getThumbnail().toString(), resultEmbed.thumbnail().get().url().get());
		assertEquals(embedSpec.getTitle(), resultEmbed.title().get());
		assertEquals(workRequest.getCommmand(), resultEmbed.description().get());
		assertEquals(embedSpec.getFields().get(0).getName(), resultEmbed.fields().get().get(0).name());
		assertEquals(embedSpec.getFields().get(0).getValue(), resultEmbed.fields().get().get(0).value());
		assertEquals(embedSpec.getFields().get(1).getName(), resultEmbed.fields().get().get(1).name());
		assertEquals(embedSpec.getFields().get(1).getValue(), resultEmbed.fields().get().get(1).value());
	}
	
	private DiscordEmbedLocaleSpec createLocaleSpec() {
		DiscordEmbedLocaleSpec spec = new DiscordEmbedLocaleSpec();
		spec.setColor(Color.BLACK.getRGB());
		spec.setThumbnail(URI.create("http://localhost"));
		
		DiscordEmbedSpec embedSpec = new DiscordEmbedSpec();
		embedSpec.setTitle("title");
		embedSpec.setDescription("%s");
		
		DiscordEmbedField embedField1 = new DiscordEmbedField();
		embedField1.setName("field 1");
		embedField1.setValue("value 1");
		DiscordEmbedField embedField2 = new DiscordEmbedField();
		embedField2.setName("field 2");
		embedField2.setValue("value 2");
		
		embedSpec.setFields(List.of(embedField1, embedField2));
		spec.setEmbedSpecs(Map.of(Language.ENGLISH, embedSpec));
		return spec;
	}
	
}
