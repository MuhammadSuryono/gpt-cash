package com.gpt.product.gpcash.chargepackage.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import com.gpt.component.idm.app.model.IDMApplicationModel;

@Entity
@Cacheable
@Table(name = "PRO_CH_PC")
public class ChargePackageModel  implements Serializable {
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
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "APP_CD", nullable = false)
	protected IDMApplicationModel application;
	
	@OneToMany(mappedBy = "chargePackage", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	protected List<ChargePackageDetailModel> chargePackageDetail;
	
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

	public IDMApplicationModel getApplication() {
		return application;
	}

	public void setApplication(IDMApplicationModel application) {
		this.application = application;
	}

	public List<ChargePackageDetailModel> getChargePackageDetail() {
		return chargePackageDetail;
	}

	public void setChargePackageDetail(List<ChargePackageDetailModel> chargePackageDetail) {
		this.chargePackageDetail = chargePackageDetail;
	}
	
}
