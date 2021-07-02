package skaro.pokedex.sdk.worker.command.ratelimit;

public class CooldownReport {

	private boolean isOnCooldown;
	private long secondsLeftInCooldown;
	
	public boolean isOnCooldown() {
		return isOnCooldown;
	}
	public void setOnCooldown(boolean isOnCooldown) {
		this.isOnCooldown = isOnCooldown;
	}
	public long getSecondsLeftInCooldown() {
		return secondsLeftInCooldown;
	}
	public void setSecondsLeftInCooldown(long secondsLeftInCooldown) {
		this.secondsLeftInCooldown = secondsLeftInCooldown;
	}
	
}
