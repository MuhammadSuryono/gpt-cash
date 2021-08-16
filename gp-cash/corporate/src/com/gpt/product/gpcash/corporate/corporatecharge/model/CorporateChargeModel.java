package com.gpt.product.gpcash.corporate.corporatecharge.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.component.idm.app.model.IDMApplicationModel;
import com.gpt.component.maintenance.parametermt.model.CurrencyModel;
import com.gpt.product.gpcash.chargepackage.model.ChargePackageModel;
import com.gpt.product.gpcash.servicecharge.model.ServiceChargeModel;

@Entity
@Cacheable
@Table(name = "CORP_CH_PC_DTL")
public class CorporateChargeModel implements Serializable{
	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(name = "ID")
	protected String id;
	
	@Column(name = "VAL_TYP")
	protected String valueType;
	
	@Column(name = "VALUE", precision=25, scale=7)
	protected BigDecimal value;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CCY_CD", nullable = false)
	protected CurrencyModel currency;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CH_PC_CD", nullable = false)
	protected ChargePackageModel chargePackage;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SRVC_CH_ID", nullable = false)
	protected ServiceChargeModel serviceCharge;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CORP_ID", nullable = false)
	protected CorporateModel corporate;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "APP_CD", nullable = false)
	protected IDMApplicationModel application;
	
	@Column(name="CREATED_BY")
	protected String createdBy;
	
	@Column(name="CREATED_DT")
	protected Timestamp createdDate;
	
	@Column(name="UPDATED_BY")
	protected String updatedBy;
	
	@Column(name="UPDATED_DT")
	protected Timestamp updatedDate;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getValueType() {
		return valueType;
	}

	public void setValueType(String valueType) {
		this.valueType = valueType;
	}

	public BigDecimal getValue() {
		return value;
	}

	public void setValue(BigDecimal value) {
		this.value = value;
	}

	public CurrencyModel getCurrency() {
		return currency;
	}

	public void setCurrency(CurrencyModel currency) {
		this.currency = currency;
	}

	public ChargePackageModel getChargePackage() {
		return chargePackage;
	}

	public void setChargePackage(ChargePackageModel chargePackage) {
		this.chargePackage = chargePackage;
	}

	public ServiceChargeModel getServiceCharge() {
		return serviceCharge;
	}

	public void setServiceCharge(ServiceChargeModel serviceCharge) {
		this.serviceCharge = serviceCharge;
	}

	public CorporateModel getCorporate() {
		return corporate;
	}

	public void setCorporate(CorporateModel corporate) {
		this.corporate = corporate;
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

	public IDMApplicationModel getApplication() {
		return application;
	}

	public void setApplication(IDMApplicationModel application) {
		this.application = application;
	}
	
}
