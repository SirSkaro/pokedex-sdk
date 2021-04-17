package skaro.pokedex.sdk.resource;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Language {
	ENGLISH("English","en"),
	SPANISH("Español", "es"),
	GERMAN("Deutsch", "de"),
	ITALIAN("Italiano", "it"),
	FRENCH("Français", "fr"),
	CHINESE_SIMPMLIFIED("简化字", "zh-Hans"),
	JAPANESE_HIR_KAT("日本語","ja-Hrkt"),
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
	
}
