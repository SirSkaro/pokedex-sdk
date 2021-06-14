package skaro.pokedex.sdk.client;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Language {
	ENGLISH("English","en"),
	SPANISH("Español", "es"),
	GERMAN("Deutsch", "de"),
	ITALIAN("Italiano", "it"),
	FRENCH("Français", "fr"),
	CHINESE_SIMPMLIFIED("简化字", "zh-Hans"),
	JAPANESE("日本語","ja-Hrkt"),
	KOREAN("한국말","ko");
	
	private String name;
	private String abbreviation;
	
	private Language(String name, String abbreviation) {
		this.name = name;
		this.abbreviation = abbreviation;
	}

	@JsonValue
	public String getName() {
		return name;
	}

	public String getAbbreviation() {
		return abbreviation;
	}
	
	@JsonIgnore
	public static Optional<Language> getLanguage(String languageToCheck) {
		Predicate<Language> matchesLanguageEnum = language -> StringUtils.equalsIgnoreCase(languageToCheck, language.name());
		Predicate<Language> matchesLanguageName = language -> StringUtils.equalsIgnoreCase(languageToCheck, language.getName());
		Predicate<Language> matchesLanguageAbbreviation = language -> StringUtils.equalsIgnoreCase(languageToCheck, language.getName());
		
		return Stream.of(Language.values())
			.filter(matchesLanguageName
					.or(matchesLanguageAbbreviation)
					.or(matchesLanguageEnum))
			.findFirst();
	}
	
}
