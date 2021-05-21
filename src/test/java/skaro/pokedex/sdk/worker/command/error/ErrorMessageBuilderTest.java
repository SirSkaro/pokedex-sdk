package skaro.pokedex.sdk.worker.command.error;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.EmbedFieldData;
import discord4j.discordjson.json.MessageCreateRequest;
import skaro.pokedex.sdk.client.Language;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequest;
import skaro.pokedex.sdk.worker.command.specification.DiscordEmbedField;
import skaro.pokedex.sdk.worker.command.specification.DiscordEmbedLocaleSpec;
import skaro.pokedex.sdk.worker.command.specification.DiscordEmbedSpec;

@ExtendWith(SpringExtension.class)
public class ErrorMessageBuilderTest {

	private DiscordEmbedLocaleSpec localeSpec;
	private ErrorMessageBuilder builder;
	
	@BeforeEach
	public void setup() {
		localeSpec = createLocaleSpec();
		builder = new ErrorMessageBuilder(localeSpec);
	}
	
	@Test
	public void testPopulateFrom() {
		String errorMessage = "You messed up";
		WorkRequest workRequest = new WorkRequest();
		workRequest.setLanguage(Language.ENGLISH);
		workRequest.setCommmand("foo-command");
		workRequest.setArguments(List.of("my", "foo", "bar"));
		ErrorMessageContent content = new ErrorMessageContent();
		content.setError(new NullPointerException(errorMessage));
		content.setWorkRequest(workRequest);
		
		MessageCreateRequest result = builder.populateFrom(content);
		EmbedData resultEmbed = result.embed().get();
		DiscordEmbedSpec embedSpec = localeSpec.getEmbedSpecs().get(workRequest.getLanguage());
		
		assertEquals(localeSpec.getColor(), resultEmbed.color().get());
		assertEquals(localeSpec.getThumbnail().toString(), resultEmbed.thumbnail().get().url().get());
		assertEquals(embedSpec.getTitle(), resultEmbed.title().get());
		assertEquals(3, resultEmbed.fields().get().size());
		
		assertEquals(embedSpec.getFields().get(0).getName(), resultEmbed.fields().get().get(0).name());
		assertEquals(errorMessage, resultEmbed.fields().get().get(0).value());
		assertUserInputFieldContents(resultEmbed.fields().get().get(1), embedSpec.getFields().get(1), workRequest);
		assertEquals(embedSpec.getFields().get(2).getName(), resultEmbed.fields().get().get(2).name());
		assertEquals(embedSpec.getFields().get(2).getValue(), resultEmbed.fields().get().get(2).value());
	}

	private DiscordEmbedLocaleSpec createLocaleSpec() {
		DiscordEmbedLocaleSpec spec = new DiscordEmbedLocaleSpec();
		spec.setColor(1);
		spec.setThumbnail(URI.create("http://localhost"));
		
		DiscordEmbedSpec embedSpec = new DiscordEmbedSpec();
		embedSpec.setTitle("title");
		embedSpec.setDescription("description");
		DiscordEmbedField technicalErrorField = new DiscordEmbedField();
		technicalErrorField.setName("Technical error");
		DiscordEmbedField userInputField = new DiscordEmbedField();
		userInputField.setName("Your input");
		DiscordEmbedField serverSupportField = new DiscordEmbedField();
		serverSupportField.setName("Server support link");
		serverSupportField.setValue("<link>");
		embedSpec.setFields(List.of(technicalErrorField, userInputField, serverSupportField));
		
		Map<Language, DiscordEmbedSpec> embedSpecs = Map.of(Language.ENGLISH, embedSpec);
		spec.setEmbedSpecs(embedSpecs);
		
		return spec;
	}
	
	private void assertUserInputFieldContents(EmbedFieldData resultEmbed, DiscordEmbedField embedFieldSpec, WorkRequest workRequest) {
		String fieldValue = resultEmbed.value();
		
		assertEquals(embedFieldSpec.getName(), resultEmbed.name());
		assertTrue(fieldValue.contains(workRequest.getCommmand()));
		workRequest.getArguments().forEach(argument -> assertTrue(fieldValue.contains(argument)));
	}
	
}
