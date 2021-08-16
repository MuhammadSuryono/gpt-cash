package com.gpt.product.gpcash.corporate.transaction.bulkpayment.model;

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

import com.gpt.component.maintenance.domesticbank.model.DomesticBankModel;
import com.gpt.component.maintenance.internationalbank.model.InternationalBankModel;
import com.gpt.component.maintenance.parametermt.model.BeneficiaryTypeModel;
import com.gpt.component.maintenance.parametermt.model.CountryModel;

@Entity
@Table(name = "TRX_BULK_PAYMENT_DTL")
public class BulkPaymentDetailModel implements Serializable {
	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(name = "ID")
	private String id;

	@Version
	private int version;

	@Column(name = "TRX_REFERENCE_NO")
	private String trxReferenceNo;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "BULK_PAYMENT_ID", nullable = false)
	private BulkPaymentModel bulkPayment;

	// START Transaction Information

	@Column(name = "BEN_ACCT_NO")
	private String benAccountNo;

	@Column(name = "BEN_ACCT_NM")
	private String benAccountName;
	
	@Column(name = "BEN_ADDRESS1")
	private String benAddress1;
	
	@Column(name = "BEN_ADDRESS2")
	private String benAddress2;
	
	@Column(name = "BEN_ADDRESS3")
	private String benAddress3;	
	
	@Column(name = "BEN_NOTIF_FLAG")
	private String benNotificationFlag;
	
	@Column(name = "BEN_NOTIF_VALUE")
	private String benNotificationValue;	

	@Column(name = "TRX_CCY_CD")
	private String trxCurrencyCode;

	@Column(name = "TRX_AMT", precision=25, scale=7)
	private BigDecimal trxAmount;

	@Column(name = "REMARK1")
	private String remark1;

	@Column(name = "REMARK2")
	private String remark2;

	@Column(name = "REMARK3")
	private String remark3;

	@Column(name = "SENDER_REF_NO")
	private String senderRefNo;

	@Column(name = "BEN_REF_NO")
	private String benRefNo;

	@Column(name = "IS_HOST_PROCESSED")
	private String isHostProcessed;
	
	@Column(name = "IS_ERROR")
	private String isError;
	
	@Column(name = "ERROR_CD")
	private String errorCode;
	
	@Column(name = "FINALIZE_FLAG")
	private String finalizeFlag;	

	// START Default Information
	@Column(name = "CREATED_BY")
	private String createdBy;	

	@Column(name = "CREATED_DT")
	private Timestamp createdDate;

	@Column(name = "UPDATED_BY")
	private String updatedBy;

	@Column(name = "UPDATED_DT")
	private Timestamp updatedDate;
	
	@Column(name = "IDX")
	private int idx;	

	
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
	

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "BEN_DOM_BANK_CD")
	protected DomesticBankModel benDomesticBankCode;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "BENEFICIARY_TYPE")
	protected BeneficiaryTypeModel benType;
	
	
	public BeneficiaryTypeModel getBenType() {
		return benType;
	}

	public void setBenType(BeneficiaryTypeModel benType) {
		this.benType = benType;
	}

	public String getLldIsBenResidence() {
		return lldIsBenResidence;
	}

	public void setLldIsBenResidence(String lldIsBenResidence) {
		this.lldIsBenResidence = lldIsBenResidence;
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

	public InternationalBankModel getBenInternationalBankCode() {
		return benInternationalBankCode;
	}

	public void setBenInternationalBankCode(InternationalBankModel benInternationalBankCode) {
		this.benInternationalBankCode = benInternationalBankCode;
	}

	public CountryModel getBenInternationalCountry() {
		return benInternationalCountry;
	}

	public void setBenInternationalCountry(CountryModel benInternationalCountry) {
		this.benInternationalCountry = benInternationalCountry;
	}

	public DomesticBankModel getBenDomesticBankCode() {
		return benDomesticBankCode;
	}

	public void setBenDomesticBankCode(DomesticBankModel benDomesticBankCode) {
		this.benDomesticBankCode = benDomesticBankCode;
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

	public String getTrxReferenceNo() {
		return trxReferenceNo;
	}

	public void setTrxReferenceNo(String trxReferenceNo) {
		this.trxReferenceNo = trxReferenceNo;
	}

	public BulkPaymentModel getBulkPayment() {
		return bulkPayment;
	}

	public void setBulkPayment(BulkPaymentModel bulkPayment) {
		this.bulkPayment = bulkPayment;
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
	
	public String getBenAddress1() {
		return benAddress1;
	}

	public void setBenAddress1(String benAddress1) {
		this.benAddress1 = benAddress1;
	}

	public String getBenAddress2() {
		return benAddress2;
	}

	public void setBenAddress2(String benAddress2) {
		this.benAddress2 = benAddress2;
	}

	public String getBenAddress3() {
		return benAddress3;
	}

	public void setBenAddress3(String benAddress3) {
		this.benAddress3 = benAddress3;
	}

	public String getTrxCurrencyCode() {
		return trxCurrencyCode;
	}

	public void setTrxCurrencyCode(String trxCurrencyCode) {
		this.trxCurrencyCode = trxCurrencyCode;
	}

	public BigDecimal getTrxAmount() {
		return trxAmount;
	}

	public void setTrxAmount(BigDecimal trxAmount) {
		this.trxAmount = trxAmount;
	}

	public String getRemark1() {
		return remark1;
	}

	public void setRemark1(String remark1) {
		this.remark1 = remark1;
	}

	public String getRemark2() {
		return remark2;
	}

	public void setRemark2(String remark2) {
		this.remark2 = remark2;
	}

	public String getRemark3() {
		return remark3;
	}

	public void setRemark3(String remark3) {
		this.remark3 = remark3;
	}

	public String getSenderRefNo() {
		return senderRefNo;
	}

	public void setSenderRefNo(String senderRefNo) {
		this.senderRefNo = senderRefNo;
	}

	public String getBenRefNo() {
		return benRefNo;
	}

	public void setBenRefNo(String benRefNo) {
		this.benRefNo = benRefNo;
	}

	public String getIsHostProcessed() {
		return isHostProcessed;
	}

	public void setIsHostProcessed(String isHostProcessed) {
		this.isHostProcessed = isHostProcessed;
	}
	
	public String getIsError() {
		return isError;
	}

	public void setIsError(String isError) {
		this.isError = isError;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getBenNotificationFlag() {
		return benNotificationFlag;
	}

	public void setBenNotificationFlag(String benNotificationFlag) {
		this.benNotificationFlag = benNotificationFlag;
	}

	public String getBenNotificationValue() {
		return benNotificationValue;
	}

	public void setBenNotificationValue(String benNotificationValue) {
		this.benNotificationValue = benNotificationValue;
	}

	public String getFinalizeFlag() {
		return finalizeFlag;
	}

	public void setFinalizeFlag(String finalizeFlag) {
		this.finalizeFlag = finalizeFlag;
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

	public int getIdx() {
		return idx;
	}

	public void setIdx(int idx) {
		this.idx = idx;
	}	
	
}
