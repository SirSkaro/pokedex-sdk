package skaro.pokedex.sdk.worker.command.ratelimit.cluster;

import static org.mockito.ArgumentMatchers.anyString;

import java.time.Duration;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.github.bucket4j.AsyncBucket;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.grid.ProxyManager;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
public class ClusteredBucketPoolTest {

	@Mock
	private ProxyManager<String> buckets;
	private ClusteredBucketPool pool;
	
	@BeforeEach
	public void setup() {
		pool = new ClusteredBucketPool(buckets);
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testGetBucket_bucketCached() {
		Bucket bucket = Mockito.mock(Bucket.class);
		AsyncBucket asyncBucket = Mockito.mock(AsyncBucket.class);
		Bandwidth bandwidth = createBandwidth();
		
		Mockito.when(buckets.getProxy(anyString(), ArgumentMatchers.any(Supplier.class)))
			.thenReturn(bucket);
		Mockito.when(bucket.asAsync())
			.thenReturn(asyncBucket);
		
		StepVerifier.create(pool.getBucket("some guild", bandwidth))
			.expectNext(asyncBucket)
			.expectComplete()
			.verify();
	}
	
	private Bandwidth createBandwidth() {
		return Bandwidth.simple(1, Duration.ofSeconds(1));
	}
}
