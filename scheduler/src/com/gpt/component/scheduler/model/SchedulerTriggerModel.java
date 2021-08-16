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
//@Cacheable can not be cacheable since we need to lock the model for clustering support
@Table(name = "SCHEDULER_TRIGGER")
public class SchedulerTriggerModel implements Serializable{
	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(name = "ID")
	protected String id;
	
	@Column(name = "IS_INACTIVE")
	protected String inactiveFlag;
	
	@Column(name = "IS_WORKING_DAY")
	protected String workingDayFlag;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TASK_CD", nullable = false)
	protected SchedulerTaskModel schedulerTask;
	
	@Column(name="CRON_EXPR")
	protected String cronExpr;
	
	@Column(name="NEXT_EXECUTION_DT")
	protected Timestamp nextExecutionDate;
	
	@Column(name="CREATED_BY")
	protected String createdBy;
	
	@Column(name="CREATED_DT")
	protected Timestamp createdDate;
	
	@Column(name="UPDATED_BY")
	protected String updatedBy;
	
	@Column(name="UPDATED_DT")
	protected Timestamp updatedDate;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getInactiveFlag() {
		return inactiveFlag;
	}

	public void setInactiveFlag(String inactiveFlag) {
		this.inactiveFlag = inactiveFlag;
	}

	public SchedulerTaskModel getSchedulerTask() {
		return schedulerTask;
	}

	public void setSchedulerTask(SchedulerTaskModel schedulerTask) {
		this.schedulerTask = schedulerTask;
	}

	public String getCronExpr() {
		return cronExpr;
	}

	public void setCronExpr(String cronExpr) {
		this.cronExpr = cronExpr;
	}
	
	public Timestamp getNextExecutionDate() {
		return nextExecutionDate;
	}
	
	public void setNextExecutionDate(Timestamp nextExecutionDate) {
		this.nextExecutionDate = nextExecutionDate;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Timestamp getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Timestamp createdDate) {
		this.createdDate = createdDate;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	public Timestamp getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(Timestamp updatedDate) {
		this.updatedDate = updatedDate;
	}

	public String getWorkingDayFlag() {
		return workingDayFlag;
	}

	public void setWorkingDayFlag(String workingDayFlag) {
		this.workingDayFlag = workingDayFlag;
	}
}