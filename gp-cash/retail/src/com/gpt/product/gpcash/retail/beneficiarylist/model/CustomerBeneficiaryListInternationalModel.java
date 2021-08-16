package com.gpt.product.gpcash.retail.beneficiarylist.model;

import java.io.Serializable;
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

import com.gpt.component.maintenance.internationalbank.model.InternationalBankModel;
import com.gpt.component.maintenance.parametermt.model.CountryModel;
import com.gpt.product.gpcash.retail.customer.model.CustomerModel;

@Entity
@Table(name = "CUST_TRX_INTERNATIONAL_BEN")
public class CustomerBeneficiaryListInternationalModel implements Serializable{
	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(name = "ID")
	protected String id;
	
	@Version
	protected int version;
	
	@Column(name = "BEN_ACCT_NO")
	protected String benAccountNo;

	@Column(name = "BEN_ACCT_NM")
	protected String benAccountName;
	
	@Column(name = "BEN_ALIAS_NM")
	protected String benAliasName;

	@Column(name = "BEN_ACCT_CCY_CD")
	protected String benAccountCurrency;
	
	@Column(name = "IDX")
	protected int idx;

	@Column(name = "IS_DELETE")
	protected String deleteFlag;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CUST_ID", nullable = false)
	protected CustomerModel customer;
	
	@Column(name = "IS_NOTIFY_BEN")
	protected String isNotifyBen;
	
	@Column(name = "EMAIL")
	protected String email;
	
	@Column(name = "BEN_ADDR_1")
	protected String benAddr1;

	@Column(name = "BEN_ADDR_2")
	protected String benAddr2;

	@Column(name = "BEN_ADDR_3")
	protected String benAddr3;
	
	@Column(name = "LLD_IS_BEN_RES")
	protected String lldIsBenResidence;

	@Column(name = "LLD_IS_BEN_CTZN")
	protected String lldIsBenCitizen;
	
	@Column(name = "LLD_IS_BEN_IDNTCL")
	protected String lldIsBenIdentical;

	@Column(name = "LLD_IS_BEN_AFFLTD")
	protected String lldIsBenAffiliated;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "LLD_BEN_RES_CTRY_CD")
	protected CountryModel lldBenResidenceCountry;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "LLD_BEN_CTZN_CTRY_CD")
	protected CountryModel lldBenCitizenCountry;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "BEN_INT_BANK_CD")
	protected InternationalBankModel benInternationalBankCode;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "BEN_INT_CTRY_CD")
	protected CountryModel benInternationalCountry;
	
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

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getBenAccountNo() {
		return benAccountNo;
	}

	public void setBenAccountNo(String benAccountNo) {
		this.benAccountNo = benAccountNo;
	}

	public String getBenAccountName() {
		return benAccountName;
	}

	public void setBenAccountName(String benAccountName) {
		this.benAccountName = benAccountName;
	}

	public String getBenAccountCurrency() {
		return benAccountCurrency;
	}

	public void setBenAccountCurrency(String benAccountCurrency) {
		this.benAccountCurrency = benAccountCurrency;
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

	public CustomerModel getCustomer() {
		return customer;
	}

	public void setCustomer(CustomerModel customer) {
		this.customer = customer;
	}

	public String getIsNotifyBen() {
		return isNotifyBen;
	}

	public void setIsNotifyBen(String isNotifyBen) {
		this.isNotifyBen = isNotifyBen;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getBenAddr1() {
		return benAddr1;
	}

	public void setBenAddr1(String benAddr1) {
		this.benAddr1 = benAddr1;
	}

	public String getBenAddr2() {
		return benAddr2;
	}

	public void setBenAddr2(String benAddr2) {
		this.benAddr2 = benAddr2;
	}

	public String getBenAddr3() {
		return benAddr3;
	}

	public void setBenAddr3(String benAddr3) {
		this.benAddr3 = benAddr3;
	}

	public String getLldIsBenResidence() {
		return lldIsBenResidence;
	}

	public void setLldIsBenResidence(String lldIsBenResidence) {
		this.lldIsBenResidence = lldIsBenResidence;
	}


	public InternationalBankModel getBenInternationalBankCode() {
		return benInternationalBankCode;
	}

	public void setBenInternationalBankCode(InternationalBankModel benInternationalBankCode) {
		this.benInternationalBankCode = benInternationalBankCode;
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

	public String getBenAliasName() {
		return benAliasName;
	}

	public void setBenAliasName(String benAliasName) {
		this.benAliasName = benAliasName;
	}

	public CountryModel getLldBenResidenceCountry() {
		return lldBenResidenceCountry;
	}

	public void setLldBenResidenceCountry(CountryModel lldBenResidenceCountry) {
		this.lldBenResidenceCountry = lldBenResidenceCountry;
	}

	public CountryModel getLldBenCitizenCountry() {
		return lldBenCitizenCountry;
	}

	public void setLldBenCitizenCountry(CountryModel lldBenCitizenCountry) {
		this.lldBenCitizenCountry = lldBenCitizenCountry;
	}

	public String getLldIsBenCitizen() {
		return lldIsBenCitizen;
	}

	public void setLldIsBenCitizen(String lldIsBenCitizen) {
		this.lldIsBenCitizen = lldIsBenCitizen;
	}

	public String getLldIsBenIdentical() {
		return lldIsBenIdentical;
	}

	public void setLldIsBenIdentical(String lldIsBenIdentical) {
		this.lldIsBenIdentical = lldIsBenIdentical;
	}

	public String getLldIsBenAffiliated() {
		return lldIsBenAffiliated;
	}

	public void setLldIsBenAffiliated(String lldIsBenAffiliated) {
		this.lldIsBenAffiliated = lldIsBenAffiliated;
	}
	
	public CountryModel getBenInternationalCountry() {
		return benInternationalCountry;
	}

	public void setBenInternationalCountry(CountryModel benInternationalCountry) {
		this.benInternationalCountry = benInternationalCountry;
	}
}
