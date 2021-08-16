package com.gpt.product.gpcash.workflow.model;

import java.sql.Timestamp;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import com.gpt.platform.cash.workflow.Status;

@Entity
@Table(name = "WE_TASK_INSTANCE", indexes = {
	@Index(name="WE_TASK_INSTANCE_INDEX1", unique = false, columnList = "IS_CANCELED, USER_, END_DT")	,
	@Index(name="WE_TASK_INSTANCE_INDEX2", unique = false, columnList = "PROCESS_INSTANCE")	
})
public class TaskInstance {
	
	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(name = "ID")
	private String id;
	
	@Column(name="STAGE_ID")
	private String stageId;

	@Enumerated
	@Column(name="STATUS")
	private Status status;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "PROCESS_INSTANCE", nullable = false)
	private ProcessInstance processInstance;
	
	@Column(name="START_DT")
	private Timestamp startDate;
	
	@Column(name="END_DT")
	private Timestamp endDate;
	
	@Column(name="USER_")
	private String user;
	
	// approval index of the approval matrix
	@Column(name="APPROVAL_LV")
	private int currApprLv;
	
	@Column(name="IS_CANCELED")
	private boolean canceled;
	
	@OneToMany(mappedBy = "taskInstance", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<Variable> variables;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Status getStatus() {
		return status;
	}
	
	public void setStatus(Status status) {
		this.status = status;
	}
	
	public String getStageId() {
		return stageId;
	}

	public void setStageId(String stageId) {
		this.stageId = stageId;
	}

	public ProcessInstance getProcessInstance() {
		return processInstance;
	}

	public void setProcessInstance(ProcessInstance processInstance) {
		this.processInstance = processInstance;
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

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}
	
	public int getCurrApprLv() {
		return currApprLv;
	}
	
	public void setCurrApprLv(int currApprLv) {
		this.currApprLv = currApprLv;
	}
	
	public boolean isCanceled() {
		return canceled;
	}
	
	public void setCanceled(boolean canceled) {
		this.canceled = canceled;
	}
	
	public Set<Variable> getVariables() {
		return variables;
	}
	
	public void setVariables(Set<Variable> variables) {
		this.variables = variables;
	}
	
}
