package com.gpt.component.maintenance.sysparam.model;

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
@Table(name="SYS_PARAMETER")
public class SysParamModel implements Serializable {
	
	@Id
	@Column(name="CD")
	protected String code;

	@Version
	protected int version;
	
	@Column(name="NM")
	protected String name;
	
	@Column(name="IDX")
	protected int idx;
	
	@Column(name="IS_DELETE")
	protected String deleteFlag;
	
	@Column(name="IS_SYSTEM")
	protected String systemFlag;
	
	@Column(name="IS_INACTIVE")
	protected String inactiveFlag;
	
	@Column(name="REGEX")
	protected String regex;
	
	@Column(name="VALUE")
	protected String value;
	
	@Column(name="CREATED_BY")
	protected String createdBy;
	
	@Column(name="CREATED_DT")
	protected Timestamp createdDate;
	
	@Column(name="UPDATED_BY")
	protected String updatedBy;
	
	@Column(name="UPDATED_DT")
	protected Timestamp updatedDate;

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

	public String getSystemFlag() {
		return systemFlag;
	}

	public void setSystemFlag(String systemFlag) {
		this.systemFlag = systemFlag;
	}

	public String getRegex() {
		return regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getInactiveFlag() {
		return inactiveFlag;
	}

	public void setInactiveFlag(String inactiveFlag) {
		this.inactiveFlag = inactiveFlag;
	}
	
	
}
