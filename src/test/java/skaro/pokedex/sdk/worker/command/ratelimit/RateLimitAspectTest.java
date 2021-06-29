package skaro.pokedex.sdk.worker.command.ratelimit;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import skaro.pokedex.sdk.discord.DiscordMessageDirector;

@ExtendWith(SpringExtension.class)
public class RateLimitAspectTest {

	private BucketPool bucketPool;
	private DiscordMessageDirector<RateLimitMessageContent> messageDirector;
	
}
