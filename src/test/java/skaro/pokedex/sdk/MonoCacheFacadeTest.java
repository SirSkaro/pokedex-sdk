package skaro.pokedex.sdk;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import reactor.test.StepVerifier;
import skaro.pokedex.sdk.client.MonoCacheFacade;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequest;

@ExtendWith(SpringExtension.class)
public class MonoCacheFacadeTest {

	@Mock
	private CacheManager cacheManager;
	private MonoCacheFacade facade;
	
	@BeforeEach
	public void setup() {
		facade = new MonoCacheFacade(cacheManager);
	}
	
	@Test
	public void testGet_cacheHit() {
		Cache workRequestCache = Mockito.mock(Cache.class);
		String key = "foo";
		WorkRequest value = new WorkRequest();
		
		Mockito.when(cacheManager.getCache(value.getClass().getName()))
			.thenReturn(workRequestCache);
		Mockito.when(workRequestCache.get(key, value.getClass()))
			.then(answer -> value);
		
		StepVerifier.create(facade.get(value.getClass(), key))
			.assertNext(request -> request.equals(value))
			.expectComplete()
			.verify();
	}
	
	@Test
	public void testGet_cacheMiss() {
		Cache workRequestCache = Mockito.mock(Cache.class);
		String key = "bar";
		
		Mockito.when(cacheManager.getCache(WorkRequest.class.getName()))
			.thenReturn(workRequestCache);
		Mockito.when(workRequestCache.get(key, WorkRequest.class))
			.thenReturn(null);
		
		StepVerifier.create(facade.get(WorkRequest.class, key))
			.expectComplete()
			.verify();
	}
	
	@Test
	public void testGet_cacheDoesntExist() {
		String key = "foo bar";
		Mockito.when(cacheManager.getCache(WorkRequest.class.getName()))
			.thenReturn(null);
		
		StepVerifier.create(facade.get(WorkRequest.class, key))
			.expectComplete()
			.verify();
	}
	
	@Test
	public void testCache() {
		Cache workRequestCache = Mockito.mock(Cache.class);
		String key = "foo";
		WorkRequest value = new WorkRequest();
		
		Mockito.when(cacheManager.getCache(value.getClass().getName()))
			.thenReturn(workRequestCache);
		
		facade.cache(key, value);
		
		Mockito.verify(workRequestCache).put(key, value);
	}
	
	@Test
	public void testCache_CacheDoesntExist() {
		Cache workRequestCache = Mockito.mock(Cache.class);
		String key = "bar";
		WorkRequest value = new WorkRequest();
		
		Mockito.when(cacheManager.getCache(value.getClass().getName()))
			.thenReturn(null);
		
		facade.cache(key, value);
		
		Mockito.verify(workRequestCache, Mockito.never()).put(key, value);
	}
	
}
