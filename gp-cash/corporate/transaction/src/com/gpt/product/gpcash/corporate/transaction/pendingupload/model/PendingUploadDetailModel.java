package com.gpt.product.gpcash.corporate.transaction.pendingupload.model;

import java.io.Serializable;
import java.math.BigDecimal;

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
@Table(name = "TRX_PENDING_UPLOAD_DTL")
public class PendingUploadDetailModel implements Serializable {

	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(name = "ID")
	private String id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PENDING_UPLOAD_ID", nullable = false)
	private PendingUploadModel parent;
	
	@Column(name = "BEN_ACCT_NO")
	private String benAccountNo;

	@Column(name = "BEN_ACCT_NM")
	private String benAccountName;

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
	
	@Column(name = "SENDER_REF_NO")
	private String senderRefNo;
	
	@Column(name = "FINALIZE_FLAG")
	private String finalizeFlag;

	@Column(name = "BEN_REF_NO")
	private String benRefNo;
	
	@Column(name = "VA_TYPE")
	private String vaType;
	
	@Column(name = "VA_NO")
	private String vaNo;
	
	@Column(name = "VA_NM")
	private String vaName;
	
	@Column(name = "VA_AMT", precision=25, scale=7)
	private BigDecimal vaAmount;

	@Column(name = "BEN_ACCT_CCY_CD")
	private String benAccountCurrencyCode;	
	
	@Column(name = "BEN_BANK_CTRY_CD")
	private String benBankCountryCode;
	
	@Column(name = "BEN_BANK_CD")
	private String benBankCode;
	
	@Column(name = "LLD_IS_BEN_RES")
	private String lldIsBenResidence;

	@Column(name = "LLD_IS_BEN_CTZN")
	private String lldIsBenCitizen;

	@Column(name = "LLD_BEN_RES_CTRY_CD")
	private String lldBenResidenceCountryCode;

	@Column(name = "LLD_BEN_CTZN_CTRY_CD")
	private String lldBenCitizenCountryCode;
	
	@Column(name = "DOM_BENEFICIARY_TYPE")
	private String beneTypeDomestic;
	
	@Column(name = "LLD_IS_BEN_IDNTCL")
	private String lldIsBenIdentical;

	@Column(name = "LLD_IS_BEN_AFFLTD")
	private String lldIsBenAffiliated;
	
	@Column(name = "UPLOAD_TYPE")
	private String uploadType;
	
	
	
	public String getUploadType() {
		return uploadType;
	}

	public void setUploadType(String uploadType) {
		this.uploadType = uploadType;
	}

	public String getBenAccountCurrencyCode() {
		return benAccountCurrencyCode;
	}

	public void setBenAccountCurrencyCode(String benAccountCurrencyCode) {
		this.benAccountCurrencyCode = benAccountCurrencyCode;
	}

	public String getBenBankCountryCode() {
		return benBankCountryCode;
	}

	public void setBenBankCountryCode(String benBankCountryCode) {
		this.benBankCountryCode = benBankCountryCode;
	}

	public String getBenBankCode() {
		return benBankCode;
	}

	public void setBenBankCode(String benBankCode) {
		this.benBankCode = benBankCode;
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

	public String getLldBenResidenceCountryCode() {
		return lldBenResidenceCountryCode;
	}

	public void setLldBenResidenceCountryCode(String lldBenResidenceCountryCode) {
		this.lldBenResidenceCountryCode = lldBenResidenceCountryCode;
	}

	public String getLldBenCitizenCountryCode() {
		return lldBenCitizenCountryCode;
	}

	public void setLldBenCitizenCountryCode(String lldBenCitizenCountryCode) {
		this.lldBenCitizenCountryCode = lldBenCitizenCountryCode;
	}

	public String getBeneTypeDomestic() {
		return beneTypeDomestic;
	}

	public void setBeneTypeDomestic(String beneTypeDomestic) {
		this.beneTypeDomestic = beneTypeDomestic;
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

	public BigDecimal getVaAmount() {
		return vaAmount;
	}

	public void setVaAmount(BigDecimal vaAmount) {
		this.vaAmount = vaAmount;
	}
	
	public String getVaType() {
		return vaType;
	}

	public void setVaType(String vaType) {
		this.vaType = vaType;
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

	@Version
	private int version;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public PendingUploadModel getParent() {
		return parent;
	}

	public void setParent(PendingUploadModel parent) {
		this.parent = parent;
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

	public String getSenderRefNo() {
		return senderRefNo;
	}

	public void setSenderRefNo(String senderRefNo) {
		this.senderRefNo = senderRefNo;
	}

	public String getFinalizeFlag() {
		return finalizeFlag;
	}

	public void setFinalizeFlag(String finalizeFlag) {
		this.finalizeFlag = finalizeFlag;
	}

	public String getBenRefNo() {
		return benRefNo;
	}

	public void setBenRefNo(String benRefNo) {
		this.benRefNo = benRefNo;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}	
}
