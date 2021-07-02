package skaro.pokedex.sdk.worker.command.ratelimit;

import io.github.bucket4j.AsyncBucket;
import io.github.bucket4j.Bandwidth;
import reactor.core.publisher.Mono;

public interface BucketPool {
	
	Mono<AsyncBucket> getBucket(String key, Bandwidth bandwidth);
	
}
