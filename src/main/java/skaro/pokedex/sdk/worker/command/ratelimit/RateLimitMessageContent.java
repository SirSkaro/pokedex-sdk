package skaro.pokedex.sdk.worker.command.ratelimit;

import skaro.pokedex.sdk.client.Language;
import skaro.pokedex.sdk.discord.MessageContent;

public class RateLimitMessageContent implements MessageContent {
	private RateLimit rateLimit;
	private String command;
	private Language language;
	private long timeLeftInSeconds;
	
	public RateLimit getRateLimit() {
		return rateLimit;
	}
	public void setRateLimit(RateLimit rateLimit) {
		this.rateLimit = rateLimit;
	}
	public String getCommand() {
		return command;
	}
	public void setCommand(String command) {
		this.command = command;
	}
	public Language getLanguage() {
		return language;
	}
	public void setLanguage(Language language) {
		this.language = language;
	}
	public long getTimeLeftInSeconds() {
		return timeLeftInSeconds;
	}
	public void setTimeLeftInSeconds(long timeLeftInSeconds) {
		this.timeLeftInSeconds = timeLeftInSeconds;
	}

}
