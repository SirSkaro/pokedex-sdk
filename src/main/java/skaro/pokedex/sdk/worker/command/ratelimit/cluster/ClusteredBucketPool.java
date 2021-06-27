package skaro.pokedex.sdk.worker.command.ratelimit.cluster;

import java.time.Duration;
import java.util.function.Supplier;

import io.github.bucket4j.AsyncBucket;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Refill;
import io.github.bucket4j.grid.ProxyManager;
import reactor.core.publisher.Mono;
import skaro.pokedex.sdk.worker.command.ratelimit.BucketPool;
import skaro.pokedex.sdk.worker.command.ratelimit.RateLimit;

public class ClusteredBucketPool implements BucketPool {
	private ProxyManager<String> buckets;
	
	public ClusteredBucketPool(ProxyManager<String> buckets) {
		this.buckets = buckets;
	}

	@Override
	public Mono<AsyncBucket> getBucket(String guildId, RateLimit rateLimit) {
		String bucketKey = createBucketKey(guildId, rateLimit);
		return Mono.fromCallable(() -> buckets.getProxy(bucketKey, supplyFallbackBucket(rateLimit)))
				.map(Bucket::asAsync);
	}

	private Supplier<BucketConfiguration> supplyFallbackBucket(RateLimit rateLimit) {
		Refill refill = Refill.intervally(rateLimit.requests(), Duration.ofSeconds(rateLimit.seconds()));
		Bandwidth bandwidth = Bandwidth.classic(rateLimit.requests(), refill);
		return () -> Bucket4j.configurationBuilder()
				.addLimit(bandwidth)
				.build();
	}
	
	private String createBucketKey(String guildId, RateLimit rateLimit) {
		String commandClassName =  rateLimit.command().getName();
		return String.format("%s-%s", commandClassName, guildId);
	}
}
