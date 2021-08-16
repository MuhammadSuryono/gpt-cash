package com.gpt.product.gpcash.corporate.workflow.model;

import java.math.BigDecimal;
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
@Table(name = "CORP_WE_TASK_INSTANCE", indexes = {
	@Index(name="CORP_WE_TASK_INSTANCE_IDX1", unique = false, columnList = "IS_CANCELED, USER_, END_DT")	,
	@Index(name="CORP_WE_TASK_INSTANCE_IDX2", unique = false, columnList = "PROCESS_INSTANCE")	
})
public class CorporateTaskInstance {
	
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
	private CorporateProcessInstance processInstance;
	
	@Column(name="START_DT")
	private Timestamp startDate;
	
	@Column(name="END_DT")
	private Timestamp endDate;
	
	@Column(name="USER_")
	private String user;
	
	// approval index of the approval matrix
	@Column(name="APPROVAL_LV")
	private int currApprLv;
	
	// approval code of the user when this task is assigned
	// mostly match the approval matrix(currApprLv) except when using "any approval level"
	@Column(name="USER_APPROVAL_LV_CODE")
	private String userApprovalLvCode;

	@Column(name="USER_APPROVAL_LV_NAME")
	private String userApprovalLvName;

	@Column(name="USER_APPROVAL_LV_ALIAS")
	private String userApprovalLvAlias;
	
	@Column(name="USER_GROUP_ID")
	private String userGroupId;
	
	@Column(name="IS_CANCELED")
	private boolean canceled;
	
	@Column(name="AMOUNT", precision=25, scale=7)
	private BigDecimal amount;
	
	@Column(name="AMOUNT_CCY_CD")
	private String amountCcyCd;
	
	@OneToMany(mappedBy = "taskInstance", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<CorporateVariable> variables;
	
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

	public CorporateProcessInstance getProcessInstance() {
		return processInstance;
	}

	public void setProcessInstance(CorporateProcessInstance processInstance) {
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
	
	public String getUserApprovalLvCode() {
		return userApprovalLvCode;
	}
	
	public void setUserApprovalLvCode(String userApprovalLvCode) {
		this.userApprovalLvCode = userApprovalLvCode;
	}
	
	public boolean isCanceled() {
		return canceled;
	}
	
	public void setCanceled(boolean canceled) {
		this.canceled = canceled;
	}
	
	public Set<CorporateVariable> getVariables() {
		return variables;
	}
	
	public void setVariables(Set<CorporateVariable> variables) {
		this.variables = variables;
	}
	
	public BigDecimal getAmount() {
		return amount;
	}
	
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	
	public String getAmountCcyCd() {
		return amountCcyCd;
	}
	
	public void setAmountCcyCd(String amountCcyCd) {
		this.amountCcyCd = amountCcyCd;
	}
	
	public String getUserApprovalLvAlias() {
		return userApprovalLvAlias;
	}
	
	public void setUserApprovalLvAlias(String userApprovalLvAlias) {
		this.userApprovalLvAlias = userApprovalLvAlias;
	}
	
	public String getUserApprovalLvName() {
		return userApprovalLvName;
	}
	
	public void setUserApprovalLvName(String userApprovalLvName) {
		this.userApprovalLvName = userApprovalLvName;
	}
	
	public String getUserGroupId() {
		return userGroupId;
	}
	
	public void setUserGroupId(String userGroupId) {
		this.userGroupId = userGroupId;
	}
}
