package com.gpt.product.gpcash.corporate.transaction.va.registration.model;

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

@Entity
@Table(name = "CORP_VA_DTL")
public class VARegistrationDetailModel implements Serializable {
	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(name = "ID")
	protected String id;
	
	@Version
	protected int version;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CORP_VA_ID", nullable = false)
	protected VARegistrationModel vaRegistration;
	
	@Column(name = "VA_NO")
	protected String vaNo;
	
	@Column(name = "VA_NM")
	protected String vaName;
	
	@Column(name = "VA_STATUS")
	protected String vaStatus;
	
	@Column(name="CREATED_BY")
	protected String createdBy;
	
	@Column(name="CREATED_DT")
	protected Timestamp createdDate;
	
	@Column(name="UPDATED_BY")
	protected String updatedBy;
	
	@Column(name="UPDATED_DT")
	protected Timestamp updatedDate;
	
	@Column(name = "IDX")
	private int idx;
	
	@Column(name = "VA_TYPE")
	protected String vaType;
	
	@Column(name = "VA_AMOUNT", precision=25, scale=7)
	protected BigDecimal vaAmount;
	
	public String getVaType() {
		return vaType;
	}
	
	public void setVaType(String vaType) {
		this.vaType = vaType;
	}
	
	public BigDecimal getVaAmount() {
		return vaAmount;
	}
	
	public void setVaAmount(BigDecimal vaAmount) {
		this.vaAmount = vaAmount;
	}
	
	public int getIdx() {
		return idx;
	}

	public void setIdx(int idx) {
		this.idx = idx;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public VARegistrationModel getVaRegistration() {
		return vaRegistration;
	}

	public void setVaRegistration(VARegistrationModel vaRegistration) {
		this.vaRegistration = vaRegistration;
	}

	public String getVaNo() {
		return vaNo;
	}

	public void setVaNo(String vaNo) {
		this.vaNo = vaNo;
	}

	public String getVaName() {
		return vaName;
	}

	public void setVaName(String vaName) {
		this.vaName = vaName;
	}

	public String getVaStatus() {
		return vaStatus;
	}

	public void setVaStatus(String vaStatus) {
		this.vaStatus = vaStatus;
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
