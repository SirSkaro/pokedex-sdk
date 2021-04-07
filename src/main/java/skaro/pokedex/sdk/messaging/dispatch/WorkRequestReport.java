package skaro.pokedex.sdk.messaging.dispatch;

import java.io.Serializable;
import java.util.Calendar;

public class WorkRequestReport implements Serializable {
	private static final long serialVersionUID = 1L;

	private AnsweredWorkRequest answeredWorkRequest;
	private Long processTime;
	private Calendar processDate;
	
	public AnsweredWorkRequest getAnsweredWorkRequest() {
		return answeredWorkRequest;
	}
	public void setAnsweredWorkRequest(AnsweredWorkRequest answeredWorkRequest) {
		this.answeredWorkRequest = answeredWorkRequest;
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
