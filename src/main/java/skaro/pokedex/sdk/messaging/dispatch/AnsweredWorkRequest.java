package skaro.pokedex.sdk.messaging.dispatch;

import java.io.Serializable;

public class AnsweredWorkRequest implements Serializable {
	private static final long serialVersionUID = 1L;

	private WorkRequest workRequest;
	private WorkStatus status;
	
	public WorkRequest getWorkRequest() {
		return workRequest;
	}
	public void setWorkRequest(WorkRequest workRequest) {
		this.workRequest = workRequest;
	}
	public WorkStatus getStatus() {
		return status;
	}
	public void setStatus(WorkStatus status) {
		this.status = status;
	}
	
}
