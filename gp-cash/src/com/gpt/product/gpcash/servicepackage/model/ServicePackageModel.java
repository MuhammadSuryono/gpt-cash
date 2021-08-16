package com.gpt.product.gpcash.servicepackage.model;

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

import com.gpt.component.idm.app.model.IDMApplicationModel;
import com.gpt.product.gpcash.chargepackage.model.ChargePackageModel;
import com.gpt.product.gpcash.limitpackage.model.LimitPackageModel;
import com.gpt.product.gpcash.menupackage.model.MenuPackageModel;

@Entity
@Cacheable
@Table(name = "PRO_SRVC_PC")
public class ServicePackageModel implements Serializable{
	
	@Id
	@Column(name = "CD")
	protected String code;

	@Version
	protected int version;

	@Column(name = "NM")
	protected String name;
	
	@Column(name = "DSCP")
	protected String dscp;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CH_PC_CD")
	protected ChargePackageModel chargePackage;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "LMT_PC_CD")
	protected LimitPackageModel limitPackage;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "MNU_PC_CD")
	protected MenuPackageModel menuPackage;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "APP_CD")
	protected IDMApplicationModel application;
	
	@Column(name = "IDX")
	protected int idx;

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

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
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

	public ChargePackageModel getChargePackage() {
		return chargePackage;
	}

	public void setChargePackage(ChargePackageModel chargePackage) {
		this.chargePackage = chargePackage;
	}

	public LimitPackageModel getLimitPackage() {
		return limitPackage;
	}

	public void setLimitPackage(LimitPackageModel limitPackage) {
		this.limitPackage = limitPackage;
	}

	public MenuPackageModel getMenuPackage() {
		return menuPackage;
	}

	public void setMenuPackage(MenuPackageModel menuPackage) {
		this.menuPackage = menuPackage;
	}

	public IDMApplicationModel getApplication() {
		return application;
	}

	public void setApplication(IDMApplicationModel application) {
		this.application = application;
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

	
}
