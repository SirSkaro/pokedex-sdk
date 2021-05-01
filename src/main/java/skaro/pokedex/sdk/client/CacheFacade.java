package skaro.pokedex.sdk.client;

import reactor.core.publisher.Mono;

public interface CacheFacade {
	
	<T> Mono<T> get(Class<T> cls, String key);
	<T> T cache(String key, T entity);
	
}
