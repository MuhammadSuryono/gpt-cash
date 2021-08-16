package com.gpt.product.gpcash.approvalmap.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.GenericGenerator;

import com.gpt.component.idm.role.model.IDMRoleModel;
import com.gpt.component.maintenance.wfrole.model.WorkflowRoleModel;
import com.gpt.product.gpcash.approvallevel.model.ApprovalLevelModel;

@Entity
@Cacheable
@Table(name = "MT_APRV_MAP")
public class ApprovalMapModel implements Serializable {
	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(name = "ID")
	protected String id;
	
	@Version
	protected int version;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ROLE_CD", nullable = false)
	protected IDMRoleModel role;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "WF_ROLE_CD", nullable = false)
	protected WorkflowRoleModel workflowRole;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "APRV_LVL_CD", nullable = false)
	protected ApprovalLevelModel approvalLevel;
	
	@Column(name = "IS_DELETE")
	protected String deleteFlag;

	@Column(name = "IS_INACTIVE")
	protected String inactiveFlag;

	@Column(name = "IS_SYSTEM")
	protected String systemFlag;

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

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public IDMRoleModel getRole() {
		return role;
	}

	public void setRole(IDMRoleModel role) {
		this.role = role;
	}

	public WorkflowRoleModel getWorkflowRole() {
		return workflowRole;
	}

	public void setWorkflowRole(WorkflowRoleModel workflowRole) {
		this.workflowRole = workflowRole;
	}

	public ApprovalLevelModel getApprovalLevel() {
		return approvalLevel;
	}

	public void setApprovalLevel(ApprovalLevelModel approvalLevel) {
		this.approvalLevel = approvalLevel;
	}

	public String getDeleteFlag() {
		return deleteFlag;
	}

	public void setDeleteFlag(String deleteFlag) {
		this.deleteFlag = deleteFlag;
	}

	public String getInactiveFlag() {
		return inactiveFlag;
	}

	public void setInactiveFlag(String inactiveFlag) {
		this.inactiveFlag = inactiveFlag;
	}

	public String getSystemFlag() {
		return systemFlag;
	}

	public void setSystemFlag(String systemFlag) {
		this.systemFlag = systemFlag;
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
	
	
}
