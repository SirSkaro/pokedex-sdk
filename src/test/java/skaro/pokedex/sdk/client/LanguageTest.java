package skaro.pokedex.sdk.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class LanguageTest {

	@Test
	public void testGetLanguage_abbreviation() {
		Language language = Language.getLanguage(Language.ENGLISH.getAbbreviation())
				.orElse(null);
		
		assertNotNull(language);
		assertEquals(Language.ENGLISH, language);
	}
	
	@Test
	public void testGetLanguage_name() {
		Language language = Language.getLanguage(Language.SPANISH.getName())
				.orElse(null);
		
		assertNotNull(language);
		assertEquals(Language.SPANISH, language);
	}
	
	@Test
	public void testGetLanguage_enumValue() {
		Language language = Language.getLanguage(Language.KOREAN.name())
				.orElse(null);
		
		assertNotNull(language);
		assertEquals(Language.KOREAN, language);
	}
	@Test
	public void testGetLanguage_emptyResult() {
		Language language = Language.getLanguage("Foo bar")
				.orElse(null);
		
		assertNull(language);
	}
	
}
