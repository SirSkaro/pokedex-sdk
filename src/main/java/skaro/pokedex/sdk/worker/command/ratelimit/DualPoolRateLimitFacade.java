package skaro.pokedex.sdk.worker.command.ratelimit;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import io.github.bucket4j.AsyncBucket;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import reactor.core.publisher.Mono;
import skaro.pokedex.sdk.worker.command.Command;

public class DualPoolRateLimitFacade implements RateLimitFacade {
	private BucketPool commandBucketPool;
	private BucketPool warningMessageBucketPool;
	private Bandwidth warningMessageBandwidth;
	
	public DualPoolRateLimitFacade(BucketPool commandBucketPool, BucketPool alertMessageBucketPool, Bandwidth alertMessageBandwidth) {
		this.commandBucketPool = commandBucketPool;
		this.warningMessageBucketPool = alertMessageBucketPool;
		this.warningMessageBandwidth = alertMessageBandwidth;
	}

	@Override
	public Mono<CooldownReport> isCommandOnCooldownForGuild(String guildId, RateLimit rateLimit) {
		String bucketKey = createBucketKey(guildId, rateLimit);
		Bandwidth bandwidth = toBandwidth(rateLimit);
		return checkBucketForAvailableToken(commandBucketPool, bucketKey, bandwidth);
	}

	@Override
	public Mono<CooldownReport> isWarningMessageOnCooldownForGuild(String guildId, Class<? extends Command> commandCls) {
		String bucketKey = createBucketKey(guildId, commandCls);
		return checkBucketForAvailableToken(warningMessageBucketPool, bucketKey, warningMessageBandwidth);
	}
	
	private Mono<CooldownReport> checkBucketForAvailableToken(BucketPool bucketPool, String bucketKey, Bandwidth bandwidth) {
		return bucketPool.getBucket(bucketKey, bandwidth)
				.flatMap(this::probeRateLimit)
				.map(this::toReport);
	}

	private Bandwidth toBandwidth(RateLimit rateLimit) {
		Refill refill = Refill.intervally(rateLimit.requests(), Duration.ofSeconds(rateLimit.seconds()));
		return Bandwidth.classic(rateLimit.requests(), refill);
	}
	
	private Mono<ConsumptionProbe> probeRateLimit(AsyncBucket bucket) {
		return Mono.fromFuture(bucket.tryConsumeAndReturnRemaining(1));
	}
	
	private String createBucketKey(String guildId, RateLimit rateLimit) {
		return createBucketKey(guildId, rateLimit.command());
	}
	
	private String createBucketKey(String guildId, Class<? extends Command> commandCls) {
		String commandClassName =  commandCls.getName();
		return String.format("%s-%s", commandClassName, guildId);
	}
	
	private CooldownReport toReport(ConsumptionProbe probe) {
		long timeLeftInSeconds = TimeUnit.SECONDS.convert(probe.getNanosToWaitForRefill(), TimeUnit.NANOSECONDS);
		boolean isOnCooldown = !probe.isConsumed();
		
		CooldownReport report = new CooldownReport();
		report.setOnCooldown(isOnCooldown);
		report.setSecondsLeftInCooldown(timeLeftInSeconds);
		return report;
	}
	
}
