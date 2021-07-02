package skaro.pokedex.sdk.worker.command.ratelimit.cluster;

import java.util.function.Supplier;

import io.github.bucket4j.AsyncBucket;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.grid.ProxyManager;
import reactor.core.publisher.Mono;
import skaro.pokedex.sdk.worker.command.ratelimit.BucketPool;

public class ClusteredBucketPool implements BucketPool {
	private ProxyManager<String> buckets;
	
	public ClusteredBucketPool(ProxyManager<String> buckets) {
		this.buckets = buckets;
	}

	@Override
	public Mono<AsyncBucket> getBucket(String key, Bandwidth bandwidth) {
		return Mono.fromCallable(() -> buckets.getProxy(key, supplyFallbackBucket(bandwidth)))
				.map(Bucket::asAsync);
	}

	private Supplier<BucketConfiguration> supplyFallbackBucket(Bandwidth bandwidth) {
		return () -> Bucket4j.configurationBuilder()
				.addLimit(bandwidth)
				.build();
	}
}
