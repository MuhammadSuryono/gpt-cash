package com.gpt.product.gpcash.workflow.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.codehaus.jackson.map.ObjectMapper;

import com.gpt.platform.cash.utils.Helper;
import com.gpt.platform.cash.workflow.Status;
import com.gpt.product.gpcash.workflow.TransactionStatus;

@Entity
@Table(name = "WE_PROCESS_INSTANCE", indexes = {
	@Index(name="WE_PROCESS_INSTANCE_INDEX1", unique = false, columnList = "CREATED_BY, END_DT")	
})
public class ProcessInstance implements Serializable {
	private static final ObjectMapper mapper = new ObjectMapper();
	
	@Id
	@Column(name = "ID")
	private String id;
	
	@Column(name="PROCESS_DEFINITION")
	private String processDefinition;
	
	@Column(name="MENU_CODE")
	private String menuCode;

	@Enumerated
	@Column(name="STATUS")
	private Status status;
	
	@Column(name="CURR_APPRV_LV")
	private int currApprLv;
	
	@Column(name="CURR_APPRV_LV_COUNT")
	private int currApprLvCount;
	
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(
		name = "WE_APPROVAL_MATRIX", 
		joinColumns = @JoinColumn(name="PROCESS_INSTANCE")
    )
	@OrderColumn(name = "IDX")
	@Column(name = "APPROVAL_MATRIX", length = 1024)
	private List<String> approvalMatrix;
	
	@Column(name="CREATED_DT")
	private Timestamp createdDate;
	
	@Column(name="CREATED_BY")
	private String createdBy;

	@Column(name="UPDATED_DT")
	private Timestamp updatedDate;
	
	@Column(name="UPDATED_BY")
	private String updatedBy;

	@Column(name="END_DT")
	private Timestamp endDate;
	
	@OneToMany(mappedBy = "processInstance", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<Variable> variables;
	
	@Column(name="CURRENT_STAGE_ID")
	private String currentStageId;

	@Transient
	private transient List<List<Object>> approvalMatrixObj;
	
	@Transient
	private transient TransactionStatus trxStatus;
	
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
	
	public int getCurrApprLv() {
		return currApprLv;
	}
	
	public void setCurrApprLv(int currApprLv) {
		this.currApprLv = currApprLv;
	}
	
	public int getCurrApprLvCount() {
		return currApprLvCount;
	}
	
	public void setCurrApprLvCount(int currApprLvCount) {
		this.currApprLvCount = currApprLvCount;
	}
	
	public String getMenuCode() {
		return menuCode;
	}
	
	public void setMenuCode(String menuCode) {
		this.menuCode = menuCode;
	}
	
	public void setApprovalMatrix(List<String> approvalMatrix) {
		this.approvalMatrix = approvalMatrix;
	}
	
	public List<String> getApprovalMatrix() {
		return approvalMatrix;
	}
	
	public void setListOfApprovalMatrix(List<List<Object>> approvalMatrix) throws Exception {
		approvalMatrixObj = approvalMatrix;
		setApprovalMatrix(Helper.chopItUp(mapper.writeValueAsString(approvalMatrix)));
	}
	
	@SuppressWarnings("unchecked")
	public List<List<Object>> getListOfApprovalMatrix() throws Exception {
		if(approvalMatrixObj == null)
			approvalMatrixObj = mapper.readValue(Helper.glueStringChopsBackTogether(getApprovalMatrix()), ArrayList.class);
		return approvalMatrixObj;
	}

	@SuppressWarnings("unchecked")
	public static List<List<Object>> getListOfApprovalMatrix(String approvalMatrix) throws Exception {
		return mapper.readValue(approvalMatrix, ArrayList.class);
	}
	
	public String getProcessDefinition() {
		return processDefinition;
	}
	
	public void setProcessDefinition(String processDefinition) {
		this.processDefinition = processDefinition;
	}
	
	public Timestamp getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Timestamp createdDate) {
		this.createdDate = createdDate;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Timestamp getEndDate() {
		return endDate;
	}

	public void setEndDate(Timestamp endDate) {
		this.endDate = endDate;
	}

	public Set<Variable> getVariables() {
		return variables;
	}

	public void setVariables(Set<Variable> variables) {
		this.variables = variables;
	}

	public String getCurrentStageId() {
		return currentStageId;
	}

	public void setCurrentStageId(String currentStageId) {
		this.currentStageId = currentStageId;
	}

	public Timestamp getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(Timestamp updatedDate) {
		this.updatedDate = updatedDate;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}
	
	public void setTrxStatus(TransactionStatus trxStatus) {
		this.trxStatus = trxStatus;
	}
	
	public TransactionStatus getTrxStatus() {
		return trxStatus;
	}
	
}
