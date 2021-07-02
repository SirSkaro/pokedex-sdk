package skaro.pokedex.sdk.worker.command.ratelimit.local;

import org.springframework.cache.Cache;

import io.github.bucket4j.AsyncBucket;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket4j;
import reactor.core.publisher.Mono;
import skaro.pokedex.sdk.worker.command.ratelimit.BucketPool;

public class LocalBucketPool implements BucketPool {
	private Cache bucketCache;
	
	public LocalBucketPool(Cache bucketCache) {
		this.bucketCache = bucketCache;
	}

	@Override
	public Mono<AsyncBucket> getBucket(String key, Bandwidth bandwidth) {
		return Mono.justOrEmpty(bucketCache.get(key, AsyncBucket.class))
				.switchIfEmpty(Mono.fromCallable(() -> cacheNewBucket(key, bandwidth)));
	}
	
	private AsyncBucket cacheNewBucket(String bucketKey, Bandwidth bandwidth) {
		AsyncBucket result = Bucket4j.builder()
			.addLimit(bandwidth)
			.build()
			.asAsync();
		
		bucketCache.put(bucketKey, result);
		return result;
	}

}
