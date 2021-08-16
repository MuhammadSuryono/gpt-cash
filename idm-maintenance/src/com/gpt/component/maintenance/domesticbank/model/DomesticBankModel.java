package com.gpt.component.maintenance.domesticbank.model;

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
@Table(name = "MT_DOM_BANK")
public class DomesticBankModel implements Serializable {
	
	@Id
	@Column(name = "CD")
	protected String code;

	@Version
	protected int version;

	@Column(name = "NM")
	protected String name;
	
	@Column(name = "ORG_UNIT_CD")
	protected String organizationUnitCode;
	
	@Column(name = "ORG_UNIT_NM")
	protected String organizationUnitName;

	@Column(name = "ADDRESS_1")
	protected String address1;
	
	@Column(name = "ADDRESS_2")
	protected String address2;
	
	@Column(name = "ADDRESS_3")
	protected String address3;
	
	@Column(name = "MEMBER_CD")
	protected String memberCode;
	
	@Column(name = "ONLINE_BNK_CONN_STS")
	protected String onlineBankConnectionStatus;
	
	@Column(name = "ONLINE_BNK_CD")
	protected String onlineBankCode;

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
	
	@Column(name="CHANNEL")
	protected String channel;
	
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

	public String getOrganizationUnitCode() {
		return organizationUnitCode;
	}

	public void setOrganizationUnitCode(String organizationUnitCode) {
		this.organizationUnitCode = organizationUnitCode;
	}

	public String getOrganizationUnitName() {
		return organizationUnitName;
	}

	public void setOrganizationUnitName(String organizationUnitName) {
		this.organizationUnitName = organizationUnitName;
	}

	public String getAddress1() {
		return address1;
	}

	public void setAddress1(String address1) {
		this.address1 = address1;
	}

	public String getAddress2() {
		return address2;
	}

	public void setAddress2(String address2) {
		this.address2 = address2;
	}

	public String getAddress3() {
		return address3;
	}

	public void setAddress3(String address3) {
		this.address3 = address3;
	}

	public String getMemberCode() {
		return memberCode;
	}

	public void setMemberCode(String memberCode) {
		this.memberCode = memberCode;
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

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getOnlineBankConnectionStatus() {
		return onlineBankConnectionStatus;
	}

	public void setOnlineBankConnectionStatus(String onlineBankConnectionStatus) {
		this.onlineBankConnectionStatus = onlineBankConnectionStatus;
	}

	public String getOnlineBankCode() {
		return onlineBankCode;
	}

	public void setOnlineBankCode(String onlineBankCode) {
		this.onlineBankCode = onlineBankCode;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

}
