package com.gpt.component.scheduler.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "SCHEDULER_STATUS")
public class SchedulerStatusModel implements Serializable{
	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(name = "ID")
	protected String id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TASK_CD", nullable = false)
	protected SchedulerTaskModel schedulerTask;
	
	@Column(name="ERROR_TRACE", length = 1024)
	protected String errorTrace;
	
	@Column(name="CREATED_BY")
	protected String createdBy;
	
	@Column(name="START_DT")
	protected Timestamp startDate;
	
	@Column(name="END_DT")
	protected Timestamp endDate;
	
	@Column(name="STATUS")
	protected String status;
	
	@Column(name = "LOG_ID")
	protected String logId;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public SchedulerTaskModel getSchedulerTask() {
		return schedulerTask;
	}

	public void setSchedulerTask(SchedulerTaskModel schedulerTask) {
		this.schedulerTask = schedulerTask;
	}

	public String getErrorTrace() {
		return errorTrace;
	}

	public void setErrorTrace(String errorTrace) {
		this.errorTrace = errorTrace;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Timestamp getStartDate() {
		return startDate;
	}

	public void setStartDate(Timestamp startDate) {
		this.startDate = startDate;
	}

	public Timestamp getEndDate() {
		return endDate;
	}

	public void setEndDate(Timestamp endDate) {
		this.endDate = endDate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getLogId() {
		return logId;
	}

	public void setLogId(String logId) {
		this.logId = logId;
	}
}