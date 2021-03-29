package skaro.pokedex.sdk.cache;

import javax.validation.constraints.Positive;

public class CacheConfigurationProperties {

	@Positive
	private Integer maxSize;
	@Positive
	private Integer timeToLive;
	
	public Integer getMaxSize() {
		return maxSize;
	}
	public void setMaxSize(Integer maxSize) {
		this.maxSize = maxSize;
	}
	public Integer getTimeToLive() {
		return timeToLive;
	}
	public void setTimeToLive(Integer timeToLive) {
		this.timeToLive = timeToLive;
	}
	
}
