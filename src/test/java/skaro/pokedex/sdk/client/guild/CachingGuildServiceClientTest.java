package skaro.pokedex.sdk.client.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static skaro.pokedex.sdk.client.guild.CachingGuildServiceClient.GUILD_SETTINGS_ENDPOINT;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import skaro.pokedex.sdk.client.CacheFacade;
import skaro.pokedex.sdk.client.Language;

@ExtendWith(SpringExtension.class)
public class CachingGuildServiceClientTest {
	@Mock
	private CacheFacade cacheFacade;
	
	private static MockWebServer mockUserSettingsServer;
	private CachingGuildServiceClient guildClient;
	private WebClient webClient;
	private ObjectMapper objectMapper;
	
	
	@BeforeAll
	public static void startMockPokeApiServer() throws IOException {
		mockUserSettingsServer = new MockWebServer();
		mockUserSettingsServer.start();
	}
	
	@BeforeEach
	public void setup() {
		objectMapper = new ObjectMapper();
		webClient = WebClient.builder()
				.baseUrl(String.format("http://localhost:%s", mockUserSettingsServer.getPort()))
				.build();
		
		guildClient = new CachingGuildServiceClient(webClient, Optional.of(cacheFacade));
	}
	
	@Test
	public void testGetSettings_settingsCached() {
		String guildId = UUID.randomUUID().toString();
		GuildSettings cachedSettings = new GuildSettings();
		Mockito.when(cacheFacade.get(GuildSettings.class, guildId))
			.thenReturn(Mono.just(cachedSettings));
		
		StepVerifier.create(guildClient.getSettings(guildId))
			.expectNext(cachedSettings)
			.expectComplete()
			.verify();
	}
	
	@Test
	public void testGetSettings_settingsNotCached() throws JsonProcessingException, InterruptedException {
		String guildId = UUID.randomUUID().toString();
		GuildSettings guildSettings = new GuildSettings();
		guildSettings.setPrefix("#");
		Mockito.when(cacheFacade.get(GuildSettings.class, guildId))
			.thenReturn(Mono.empty());
		Mockito.when(cacheFacade.cache(eq(guildId), any(GuildSettings.class)))
			.thenAnswer(answer -> answer.getArgument(1));
		mockUserSettingsServer.enqueue(createMockResponseWithBody(guildSettings));
		
		StepVerifier.create(guildClient.getSettings(guildId))
			.assertNext(settings -> assertEquals(guildSettings.getPrefix(), settings.getPrefix()))
			.expectComplete()
			.verify();
		
		String expectedEndpoint = String.format("%s/%s", GUILD_SETTINGS_ENDPOINT, guildId);
		RecordedRequest recordedRequest = mockUserSettingsServer.takeRequest();
		assertEquals(HttpMethod.GET.toString(), recordedRequest.getMethod());
		assertEquals(expectedEndpoint, recordedRequest.getPath());
	}
	
	@Test
	public void testSaveSettings() throws JsonProcessingException, InterruptedException {
		String guildId = UUID.randomUUID().toString();
		GuildSettings newSettings = new GuildSettings();
		newSettings.setPrefix("%");
		newSettings.setLanguage(Language.CHINESE_SIMPMLIFIED);
		
		Mockito.when(cacheFacade.cache(eq(guildId), any(GuildSettings.class)))
			.thenAnswer(answer -> answer.getArgument(1));
		mockUserSettingsServer.enqueue(createMockResponseWithBody(newSettings));
		
		Consumer<GuildSettings> assertPrefixAndLanguage = settings -> {
			assertEquals(newSettings.getPrefix(), settings.getPrefix());
			assertEquals(newSettings.getLanguage(), settings.getLanguage());
			
		};
		
		StepVerifier.create(guildClient.saveSettings(guildId, newSettings))
			.assertNext(assertPrefixAndLanguage)
			.expectComplete()
			.verify();
		
		String expectedEndpoint = String.format("%s/%s", GUILD_SETTINGS_ENDPOINT, guildId);
		RecordedRequest recordedRequest = mockUserSettingsServer.takeRequest();
		assertEquals(HttpMethod.PUT.toString(), recordedRequest.getMethod());
		assertEquals(expectedEndpoint, recordedRequest.getPath());
	}
	
	private MockResponse createMockResponseWithBody(Object body) throws JsonProcessingException {
		return new MockResponse()
				.setBody(objectMapper.writeValueAsString(body))
				.addHeader("Content-Type", "application/json"); 
	}
	
}
