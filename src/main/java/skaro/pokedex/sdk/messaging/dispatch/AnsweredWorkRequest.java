package skaro.pokedex.sdk.messaging.dispatch;

import java.io.Serializable;
import java.util.Calendar;

public class AnsweredWorkRequest implements Serializable {
	private static final long serialVersionUID = 1L;

	private WorkRequest workRequest;
	private Long processTime;
	private Calendar processDate;
	
	public WorkRequest getWorkRequest() {
		return workRequest;
	}
	public void setWorkRequest(WorkRequest workRequest) {
		this.workRequest = workRequest;
	}
	public Long getProcessTime() {
		return processTime;
	}
	public void setProcessTime(Long processTime) {
		this.processTime = processTime;
	}
	public Calendar getProcessDate() {
		return processDate;
	}
	public void setProcessDate(Calendar processDate) {
		this.processDate = processDate;
	}
	
}
