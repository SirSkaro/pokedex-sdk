package skaro.pokedex.sdk.worker.command.specification;

import java.util.List;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class DiscordEmbedSpec {

	@NotEmpty
	private String title;
	@NotEmpty
	private String description;
	@NotNull
	private List<@NotNull DiscordEmbedField> fields;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public List<DiscordEmbedField> getFields() {
		return fields;
	}
	public void setFields(List<DiscordEmbedField> fields) {
		this.fields = fields;
	}
	
}
