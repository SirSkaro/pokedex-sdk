package skaro.pokedex.sdk.worker.command.ratelimit.local;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.cache.Cache;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.github.bucket4j.AsyncBucket;
import io.github.bucket4j.Bandwidth;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
public class LocalBucketPoolTest {

	@Mock
	private Cache bucketCache;
	private LocalBucketPool pool;
	
	@BeforeEach
	public void setup() {
		pool = new LocalBucketPool(bucketCache);
	}
	
	@Test
	public void testGetBucket_bucketNotCached() {
		Bandwidth bandwidth = createBandwidth();
		
		StepVerifier.create(pool.getBucket("my guild", bandwidth))
			.assertNext(bucket -> assertNotNull(bucket))
			.expectComplete()
			.verify();
		
		Mockito.verify(bucketCache).put(any(), any(AsyncBucket.class));
	}
	
	@Test
	public void testGetBucket_bucketCached() {
		AsyncBucket bucket = Mockito.mock(AsyncBucket.class);
		Bandwidth bandwidth = createBandwidth();
		String guildId = "guildId";
		Mockito.when(bucketCache.get(anyString(), eq(AsyncBucket.class)))
			.thenReturn(bucket);
		
		StepVerifier.create(pool.getBucket(guildId, bandwidth))
			.expectNext(bucket)
			.expectComplete()
			.verify();
	}
	
	private Bandwidth createBandwidth() {
		return Bandwidth.simple(1, Duration.ofSeconds(1));
	}
	
}
