package com.gpt.product.gpcash.maintenance.timedepositretail.specialrate.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

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

import com.gpt.product.gpcash.retail.customer.model.CustomerModel;

@Entity
@Table(name = "MT_CUST_TD_SPECIAL_RT")
public class CustomerTimeDepositSpecialRateModel implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6733559344385243657L;

	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(name = "ID")
	protected String id;

	@Version
	protected int version;

	@Column(name = "IDX")
	protected int idx;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CUST_ID")
	protected CustomerModel customer;
	
	@Column(name="REFNO_SPECIALRATE")
	protected String refNoSpecialRate;
	
	@Column(name = "SPECIAL_RT", precision=25, scale=7)
	protected BigDecimal specialRate;
	
	@Column(name = "EXPIRY_DT")
	protected Timestamp expiryDate;

	@Column(name = "STATUS")
	protected String status;
	
	@Column(name="CREATED_BY")
	protected String createdBy;
	
	@Column(name="CREATED_DT")
	protected Timestamp createdDate;
	
	@Column(name="UPDATED_BY")
	protected String updatedBy;
	
	@Column(name="UPDATED_DT")
	protected Timestamp updatedDate;
	
	@Column(name = "IS_DELETE")
	protected String deleteFlag;
	
	@Column(name = "IS_PENDING_TRX")
	protected String pendingTaskTrxFlag;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getIdx() {
		return idx;
	}

	
	public String getRefNoSpecialRate() {
		return refNoSpecialRate;
	}

	public void setRefNoSpecialRate(String refNoSpecialRate) {
		this.refNoSpecialRate = refNoSpecialRate;
	}

	public void setIdx(int idx) {
		this.idx = idx;
	}

	public Timestamp getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(Timestamp expiryDate) {
		this.expiryDate = expiryDate;
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

	public String getDeleteFlag() {
		return deleteFlag;
	}

	public void setDeleteFlag(String deleteFlag) {
		this.deleteFlag = deleteFlag;
	}
	

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getPendingTaskTrxFlag() {
		return pendingTaskTrxFlag;
	}

	public void setPendingTaskTrxFlag(String pendingTaskTrxFlag) {
		this.pendingTaskTrxFlag = pendingTaskTrxFlag;
	}

	public BigDecimal getSpecialRate() {
		return specialRate;
	}

	public void setSpecialRate(BigDecimal specialRate) {
		this.specialRate = specialRate;
	}

	public CustomerModel getCustomer() {
		return customer;
	}

	public void setCustomer(CustomerModel customer) {
		this.customer = customer;
	}
	
	
}
