package skaro.pokedex.sdk.worker.command.ratelimit.cluster;

import static org.mockito.ArgumentMatchers.anyString;

import java.lang.annotation.Annotation;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.github.bucket4j.AsyncBucket;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.grid.ProxyManager;
import reactor.test.StepVerifier;
import skaro.pokedex.sdk.worker.command.Command;
import skaro.pokedex.sdk.worker.command.ratelimit.RateLimit;

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
		RateLimit rateLimit = createRatelimit();
		
		Mockito.when(buckets.getProxy(anyString(), ArgumentMatchers.any(Supplier.class)))
			.thenReturn(bucket);
		Mockito.when(bucket.asAsync())
			.thenReturn(asyncBucket);
		
		StepVerifier.create(pool.getBucket("some guild", rateLimit))
			.expectNext(asyncBucket)
			.expectComplete()
			.verify();
	}
	
	private RateLimit createRatelimit() {
		return new RateLimit() {
			@Override
			public Class<? extends Annotation> annotationType() {
				return this.getClass();
			}

			@Override
			public int requests() {
				return 1;
			}

			@Override
			public int seconds() {
				return 1;
			}

			@Override
			public Class<? extends Command> command() {
				return Command.class;
			}
		};
	}
}
