package skaro.pokedex.sdk.cache;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

public class DistributedCacheConfigurationProperties {

	@NotEmpty
	private List<@NotBlank String> nodeAddresses;
	@NotBlank
	private String clusterName;

	public List<String> getNodeAddresses() {
		return nodeAddresses;
	}
	public void setNodeAddresses(List<String> nodeAddresses) {
		this.nodeAddresses = nodeAddresses;
	}
	public String getClusterName() {
		return clusterName;
	}
	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}
}
