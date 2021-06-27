package skaro.pokedex.sdk.worker.command.ratelimit.local;

import java.time.Duration;

import org.springframework.cache.Cache;

import io.github.bucket4j.AsyncBucket;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import reactor.core.publisher.Mono;
import skaro.pokedex.sdk.worker.command.ratelimit.BucketPool;
import skaro.pokedex.sdk.worker.command.ratelimit.RateLimit;

public class LocalBucketPool implements BucketPool {

	private Cache bucketCache;
	
	public LocalBucketPool(Cache bucketCache) {
		this.bucketCache = bucketCache;
	}

	@Override
	public Mono<AsyncBucket> getBucket(String guildId, RateLimit rateLimit) {
		String bucketKey = createBucketKey(guildId, rateLimit);
		return Mono.justOrEmpty(bucketCache.get(bucketKey, AsyncBucket.class))
				.switchIfEmpty(Mono.fromCallable(() -> cacheNewBucket(bucketKey, rateLimit)));
	}
	
	private AsyncBucket cacheNewBucket(String bucketKey, RateLimit rateLimit) {
		Refill refill = Refill.intervally(rateLimit.requests(), Duration.ofSeconds(rateLimit.seconds()));
		AsyncBucket result = Bucket4j.builder()
			.addLimit(Bandwidth.classic(rateLimit.requests(), refill))
			.build()
			.asAsync();
		
		bucketCache.put(bucketKey, result);
		return result;
	}

	private String createBucketKey(String guildId, RateLimit rateLimit) {
		String commandClassName =  rateLimit.command().getName();
		return String.format("%s-%s", commandClassName, guildId);
	}
	
}
