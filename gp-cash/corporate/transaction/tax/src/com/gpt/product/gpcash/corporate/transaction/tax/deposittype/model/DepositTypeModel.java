package com.gpt.product.gpcash.corporate.transaction.tax.deposittype.model;

import java.io.Serializable;
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

import com.gpt.product.gpcash.corporate.transaction.tax.taxtype.model.TaxTypeModel;

@Entity
@Cacheable
@Table(name = "MT_DEPOSIT_TYP")
public class DepositTypeModel implements Serializable {
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
	
	@Column(name = "DSCP")
	public String dscp;

	@Column(name = "IDX")
	public int idx;

	@Column(name = "IS_DELETE")
	public String deleteFlag;

	@Column(name = "IS_INACTIVE")
	public String inactiveFlag;

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
	
	@Column(name = "TAX_DEPOSIT_CD")
	public String taxDepositCode;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TAX_TYP_CD")
	public TaxTypeModel taxType;

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

	public String getDscp() {
		return dscp;
	}

	public void setDscp(String dscp) {
		this.dscp = dscp;
	}

	public int getIdx() {
		return idx;
	}

	public void setIdx(int idx) {
		this.idx = idx;
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

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public TaxTypeModel getTaxType() {
		return taxType;
	}

	public void setTaxType(TaxTypeModel taxType) {
		this.taxType = taxType;
	}

	public String getTaxDepositCode() {
		return taxDepositCode;
	}

	public void setTaxDepositCode(String taxDepositCode) {
		this.taxDepositCode = taxDepositCode;
	}

}
