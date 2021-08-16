package com.gpt.product.gpcash.service.model;

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

import com.gpt.component.idm.menu.model.IDMMenuModel;
import com.gpt.product.gpcash.servicetype.model.ServiceTypeModel;

@Entity
@Cacheable
@Table(name = "PRO_SRVC")
public class ServiceModel implements Serializable {
	@Id
	@Column(name = "CD")
	protected String code;

	@Version
	protected int version;

	@Column(name = "NM")
	protected String name;
	
	@Column(name = "DSCP")
	protected String dscp;

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
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SRVC_TYP_CD", nullable = false)
	protected ServiceTypeModel serviceType;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "MENU_CD")
	protected IDMMenuModel menu;
	
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

	public ServiceTypeModel getServiceType() {
		return serviceType;
	}

	public void setServiceType(ServiceTypeModel serviceType) {
		this.serviceType = serviceType;
	}

	public String getDscp() {
		return dscp;
	}

	public void setDscp(String dscp) {
		this.dscp = dscp;
	}

	public IDMMenuModel getMenu() {
		return menu;
	}

	public void setMenu(IDMMenuModel menu) {
		this.menu = menu;
	}
}
