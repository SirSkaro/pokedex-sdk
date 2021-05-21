package skaro.pokedex.sdk.worker.command.validation.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.MessageCreateRequest;
import discord4j.rest.util.Color;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
import skaro.pokedex.sdk.client.Language;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequest;
import skaro.pokedex.sdk.worker.command.specification.DiscordEmbedLocaleSpec;
import skaro.pokedex.sdk.worker.command.specification.DiscordEmbedSpec;

@ExtendWith(SpringExtension.class)
public class DiscordPermissionsMessageBuilderTest {

	private DiscordEmbedLocaleSpec localeSpec;
	private DiscordPermissionsMessageBuilder builder;
	
	@BeforeEach
	public void setup() {
		localeSpec = createLocaleSpec();
		builder = new DiscordPermissionsMessageBuilder(localeSpec);
	}
	
	@Test
	public void testPopulateForm() {
		WorkRequest workRequest = new WorkRequest();
		workRequest.setLanguage(Language.ENGLISH);
		PermissionSet requiredPermissions = PermissionSet.of(Permission.ADD_REACTIONS, Permission.MOVE_MEMBERS);
		DiscordPermissionsMessageContent content = new DiscordPermissionsMessageContent();
		content.setWorkRequest(workRequest);
		content.setRequiredPermissions(requiredPermissions);
		
		MessageCreateRequest result = builder.populateFrom(content);
		EmbedData resultEmbed = result.embed().get();
		DiscordEmbedSpec embedSpec = localeSpec.getEmbedSpecs().get(workRequest.getLanguage());
		
		assertEmbedCharacteristics(resultEmbed, embedSpec);
		assertDescriptionContainsRequiredPermissions(resultEmbed, requiredPermissions);
	}
	
	private DiscordEmbedLocaleSpec createLocaleSpec() {
		DiscordEmbedLocaleSpec spec = new DiscordEmbedLocaleSpec();
		spec.setColor(Color.RED.getRGB());
		spec.setThumbnail(URI.create("http://localhost"));
		
		DiscordEmbedSpec embedSpec = new DiscordEmbedSpec();
		embedSpec.setTitle("title");
		embedSpec.setDescription("%s");
		
		spec.setEmbedSpecs(Map.of(Language.ENGLISH, embedSpec));
		return spec;
	}
	
	private void assertEmbedCharacteristics(EmbedData resultEmbed, DiscordEmbedSpec embedSpec) {
		assertEquals(localeSpec.getColor(), resultEmbed.color().get());
		assertEquals(localeSpec.getThumbnail().toString(), resultEmbed.thumbnail().get().url().get());
		assertEquals(embedSpec.getTitle(), resultEmbed.title().get());
	}
	
	private void assertDescriptionContainsRequiredPermissions(EmbedData resultEmbed, PermissionSet requiredPermissions) {
		String description = resultEmbed.description().get();
		requiredPermissions.forEach(permission -> assertTrue(description.contains(permission.toString())));
	}
}
