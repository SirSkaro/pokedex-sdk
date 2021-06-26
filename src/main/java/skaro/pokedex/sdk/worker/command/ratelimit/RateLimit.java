package skaro.pokedex.sdk.worker.command.ratelimit;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import skaro.pokedex.sdk.worker.command.Command;

@Retention(RUNTIME)
@Target(TYPE)
public @interface RateLimit {
	int requests();
	int seconds();
	Class<? extends Command> command();
}
