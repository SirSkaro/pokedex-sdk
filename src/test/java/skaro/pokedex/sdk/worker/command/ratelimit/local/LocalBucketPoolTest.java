package skaro.pokedex.sdk.worker.command.ratelimit.local;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import java.lang.annotation.Annotation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.cache.Cache;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.github.bucket4j.AsyncBucket;
import reactor.test.StepVerifier;
import skaro.pokedex.sdk.worker.command.Command;
import skaro.pokedex.sdk.worker.command.ratelimit.RateLimit;

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
		RateLimit rateLimit = createRatelimit();
		
		StepVerifier.create(pool.getBucket("my guild", rateLimit))
			.assertNext(bucket -> assertNotNull(bucket))
			.expectComplete()
			.verify();
		
		Mockito.verify(bucketCache).put(any(), any(AsyncBucket.class));
	}
	
	@Test
	public void testGetBucket_bucketCached() {
		AsyncBucket bucket = Mockito.mock(AsyncBucket.class);
		RateLimit rateLimit = createRatelimit();
		String guildId = "guildId";
		Mockito.when(bucketCache.get(anyString(), eq(AsyncBucket.class)))
			.thenReturn(bucket);
		
		StepVerifier.create(pool.getBucket(guildId, rateLimit))
			.expectNext(bucket)
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
