package com.gpt.component.maintenance.parametermt.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Cacheable
@Table(name="MT_PARAMETER")
public class ParameterMaintenanceModel implements Serializable {
	/**
	 * MUST use protected for parameter maintenance model if not got error using clazz.getDeclaredField in
	 * ParameterMaintenanceServiceImpl 
	 */
	
	@Id
	@Column(name="CD")
	public String code;
	
	@Version
	public int version;
	
	@Column(name="NM")
	public String name;
	
	@Column(name="IDX")
	public int idx;
	
	@Column(name="IS_DELETE")
	public String deleteFlag;
	
	@Column(name="IS_INACTIVE")
	public String inactiveFlag;
	
	@Column(name="IS_SYSTEM")
	public String systemFlag;
	
	@Column(name="DSCP")
	public String description;
	
	@Column(name="MODEL_NM")
	public String modelName;
	
	@Column(name="PARAM_TYP_MODEL_CD")
	public String parentModelCode;
	
	@Column(name="PARAM_TYP_MODEL_NM")
	public String parentModelName;
	
	@Column(name="PARENT_NM")
	public String parentName;
	
	@Column(name="APP")
	public String applicationCode;
	
	@Column(name="PARENT_PROPERTY")
	public String parentProperty;
	
	@Column(name="CREATED_BY")
	public String createdBy;
	
	@Column(name="CREATED_DT")
	public Timestamp createdDate;
	
	@Column(name="UPDATED_BY")
	public String updatedBy;
	
	@Column(name="UPDATED_DT")
	public Timestamp updatedDate;

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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getModelName() {
		return modelName;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public String getParentModelName() {
		return parentModelName;
	}

	public void setParentModelName(String parentModelName) {
		this.parentModelName = parentModelName;
	}

	public String getParentName() {
		return parentName;
	}

	public void setParentName(String parentName) {
		this.parentName = parentName;
	}

	public String getApplicationCode() {
		return applicationCode;
	}

	public void setApplicationCode(String applicationCode) {
		this.applicationCode = applicationCode;
	}

	public String getParentProperty() {
		return parentProperty;
	}

	public void setParentProperty(String parentProperty) {
		this.parentProperty = parentProperty;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getParentModelCode() {
		return parentModelCode;
	}

	public void setParentModelCode(String parentModelCode) {
		this.parentModelCode = parentModelCode;
	}
}
