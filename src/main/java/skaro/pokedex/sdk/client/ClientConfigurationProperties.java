package skaro.pokedex.sdk.client;

import java.net.URI;

import javax.validation.constraints.NotNull;

public class ClientConfigurationProperties {

	@NotNull
	private URI baseUri;

	public URI getBaseUri() {
		return baseUri;
	}
	public void setBaseUri(URI baseUri) {
		this.baseUri = baseUri;
	}
	
}
