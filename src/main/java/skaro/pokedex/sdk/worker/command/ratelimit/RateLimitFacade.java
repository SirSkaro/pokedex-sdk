package skaro.pokedex.sdk.worker.command.ratelimit;

import reactor.core.publisher.Mono;
import skaro.pokedex.sdk.worker.command.Command;

public interface RateLimitFacade {

	Mono<CooldownReport> isCommandOnCooldownForGuild(String guildId, RateLimit rateLimit);
	Mono<CooldownReport> isWarningMessageOnCooldownForGuild(String guildId, Class<? extends Command> commandCls);
	
}
