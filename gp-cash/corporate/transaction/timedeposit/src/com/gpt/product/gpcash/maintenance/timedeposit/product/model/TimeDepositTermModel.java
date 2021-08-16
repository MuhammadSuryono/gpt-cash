package com.gpt.product.gpcash.maintenance.timedeposit.product.model;

import java.sql.Timestamp;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import com.gpt.component.maintenance.parametermt.model.PrepaidDenominationTypeModel;

@Entity
@Cacheable
@Table(name = "MT_TD_TERM")
public class TimeDepositTermModel {
	/**
	 * MUST use protected for parameter maintenance model if not got error using clazz.getDeclaredField in
	 * ParameterMaintenanceServiceImpl 
	 */
	
	@Id
	@Column(name = "CD")
	public String code;

	@Version
	public int version;

	@Column(name = "NM")
	public String name;
	
	@Column(name = "TYPE")
	public String termType;
	
	@Column(name = "PARAM")
	public int termParam;

	@Column(name = "IS_DELETE")
	public String deleteFlag;

	@Column(name = "IS_SYSTEM")
	public String systemFlag;
	
	@Column(name="CREATED_BY")
	public String createdBy;
	
	@Column(name="CREATED_DT")
	public Timestamp createdDate;
	
	@Column(name="UPDATED_BY")
	public String updatedBy;
	
	@Column(name="UPDATED_DT")
	public Timestamp updatedDate;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PRODUCT_CD")
	public TimeDepositProductModel productType;
	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDeleteFlag() {
		return deleteFlag;
	}

	public void setDeleteFlag(String deleteFlag) {
		this.deleteFlag = deleteFlag;
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

	public String getTermType() {
		return termType;
	}

	public void setTermType(String termType) {
		this.termType = termType;
	}

	public int getTermParam() {
		return termParam;
	}

	public void setTermParam(int termParam) {
		this.termParam = termParam;
	}

	public TimeDepositProductModel getProductType() {
		return productType;
	}

	public void setProductType(TimeDepositProductModel productType) {
		this.productType = productType;
	}

}
