package com.gpt.product.gpcash.corporate.transaction.report.domtrxreport.bean;

import java.math.BigDecimal;
import java.sql.Timestamp;

import com.gpt.platform.cash.constants.ApplicationConstants;

public class DomReportBean {
	private String no;
	private String refNo;
	private String hostRefNo;
	private String retrievalRefNo;
	private Timestamp createdDt;
	private Timestamp instructionDt;
	private String instructionMode;
	private String status;
	private String recurring;
	private String reccuringEvery;
	private Timestamp reccurringStartDt;
	private Timestamp reccurringEndDt;
	private String srvcCd;
	private String corpId;
	private String userId;
	private String sourceAcctNo;
	private String sourceAcctNm;
	private String benAcctNo;
	private String benAcctNm;
	private String benAcctAlias;
	private String benAddress1;
	private String benAddress2;
	private String benAddress3;
	private String benBankName;
	private String benBankCd;
	private String chargeIns;
	private String currency;
	private BigDecimal trxAmt;
	private BigDecimal debitAmt;
	private BigDecimal fee;
	private BigDecimal totalAmt;
	private String desc;
	private String senderRefNo;
	private String benRefNo;
	private String isFinal;
	private String isResident;
	private String resident;
	private String isCitizen;
	private String citizen;
	private String benType;
	private String isNotifyBen;
	private String notifyBenValue;
	private String fileFormat;
	private String delimiter = ApplicationConstants.DELIMITER_COMMA;
	
	public String getNo() {
		return no;
	}
	public void setNo(String no) {
		this.no = no;
	}
	public String getRefNo() {
		return refNo;
	}
	public void setRefNo(String refNo) {
		this.refNo = refNo;
	}
	public String getHostRefNo() {
		return hostRefNo;
	}
	public void setHostRefNo(String hostRefNo) {
		this.hostRefNo = hostRefNo;
	}
	public Timestamp getCreatedDt() {
		return createdDt;
	}
	public void setCreatedDt(Timestamp createdDt) {
		this.createdDt = createdDt;
	}
	public Timestamp getInstructionDt() {
		return instructionDt;
	}
	public void setInstructionDt(Timestamp instructionDt) {
		this.instructionDt = instructionDt;
	}
	public String getInstructionMode() {
		return instructionMode;
	}
	public void setInstructionMode(String instructionMode) {
		this.instructionMode = instructionMode;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getRecurring() {
		return recurring;
	}
	public void setRecurring(String recurring) {
		this.recurring = recurring;
	}
	public String getReccuringEvery() {
		return reccuringEvery;
	}
	public void setReccuringEvery(String reccuringEvery) {
		this.reccuringEvery = reccuringEvery;
	}
	public Timestamp getReccurringStartDt() {
		return reccurringStartDt;
	}
	public void setReccurringStartDt(Timestamp reccurringStartDt) {
		this.reccurringStartDt = reccurringStartDt;
	}
	public Timestamp getReccurringEndDt() {
		return reccurringEndDt;
	}
	public void setReccurringEndDt(Timestamp reccurringEndDt) {
		this.reccurringEndDt = reccurringEndDt;
	}
	public String getSrvcCd() {
		return srvcCd;
	}
	public void setSrvcCd(String srvcCd) {
		this.srvcCd = srvcCd;
	}
	public String getCorpId() {
		return corpId;
	}
	public void setCorpId(String corpId) {
		this.corpId = corpId;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getSourceAcctNo() {
		return sourceAcctNo;
	}
	public void setSourceAcctNo(String sourceAcctNo) {
		this.sourceAcctNo = sourceAcctNo;
	}
	public String getSourceAcctNm() {
		return sourceAcctNm;
	}
	public void setSourceAcctNm(String sourceAcctNm) {
		this.sourceAcctNm = sourceAcctNm;
	}
	public String getBenAcctNo() {
		return benAcctNo;
	}
	public void setBenAcctNo(String benAcctNo) {
		this.benAcctNo = benAcctNo;
	}
	public String getBenAcctNm() {
		return benAcctNm;
	}
	public void setBenAcctNm(String benAcctNm) {
		this.benAcctNm = benAcctNm;
	}
	public String getBenAcctAlias() {
		return benAcctAlias;
	}
	public void setBenAcctAlias(String benAcctAlias) {
		this.benAcctAlias = benAcctAlias;
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
	public String getBenBankName() {
		return benBankName;
	}
	public void setBenBankName(String benBankName) {
		this.benBankName = benBankName;
	}
	public String getBenBankCd() {
		return benBankCd;
	}
	public void setBenBankCd(String benBankCd) {
		this.benBankCd = benBankCd;
	}
	public String getChargeIns() {
		return chargeIns;
	}
	public void setChargeIns(String chargeIns) {
		this.chargeIns = chargeIns;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	
	public BigDecimal getTrxAmt() {
		return trxAmt;
	}
	public void setTrxAmt(BigDecimal trxAmt) {
		this.trxAmt = trxAmt;
	}
	public BigDecimal getDebitAmt() {
		return debitAmt;
	}
	public void setDebitAmt(BigDecimal debitAmt) {
		this.debitAmt = debitAmt;
	}
	public BigDecimal getFee() {
		return fee;
	}
	public void setFee(BigDecimal fee) {
		this.fee = fee;
	}
	public BigDecimal getTotalAmt() {
		return totalAmt;
	}
	public void setTotalAmt(BigDecimal totalAmt) {
		this.totalAmt = totalAmt;
	}
	public String getDelimiter() {
		return delimiter;
	}
	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
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
	public String getIsFinal() {
		return isFinal;
	}
	public void setIsFinal(String isFinal) {
		this.isFinal = isFinal;
	}
	public String getIsResident() {
		return isResident;
	}
	public void setIsResident(String isResident) {
		this.isResident = isResident;
	}
	public String getResident() {
		return resident;
	}
	public void setResident(String resident) {
		this.resident = resident;
	}
	public String getIsCitizen() {
		return isCitizen;
	}
	public void setIsCitizen(String isCitizen) {
		this.isCitizen = isCitizen;
	}
	public String getCitizen() {
		return citizen;
	}
	public void setCitizen(String citizen) {
		this.citizen = citizen;
	}
	public String getBenType() {
		return benType;
	}
	public void setBenType(String benType) {
		this.benType = benType;
	}
	public String getIsNotifyBen() {
		return isNotifyBen;
	}
	public void setIsNotifyBen(String isNotifyBen) {
		this.isNotifyBen = isNotifyBen;
	}
	public String getNotifyBenValue() {
		return notifyBenValue;
	}
	public void setNotifyBenValue(String notifyBenValue) {
		this.notifyBenValue = notifyBenValue;
	}
	
	public String getRetrievalRefNo() {
		return retrievalRefNo;
	}
	public void setRetrievalRefNo(String retrievalRefNo) {
		this.retrievalRefNo = retrievalRefNo;
	}
	
	public String getFileFormat() {
		return fileFormat;
	}
	public void setFileFormat(String fileFormat) {
		if(fileFormat!= null 
				&& !fileFormat.equals(ApplicationConstants.EMPTY_STRING)
				&& ApplicationConstants.FILE_FORMAT_TXT.equals(fileFormat)) {
			delimiter = ApplicationConstants.DELIMITER_PIPE;
		}
		
		this.fileFormat = fileFormat;
	}
	public String getHeader() {
		return "No"+ delimiter
				+ "Transaction Reference Number"+ delimiter
				+ "Reference Number Interface"+ delimiter
				+ "Reference Number Host"+ delimiter
				+ "Tanggal Pembuatan"+ delimiter
				+ "Instruction Date"+ delimiter
				+ "Instruction Mode"+ delimiter
				+ "Status"+ delimiter
				+ "Berkala"+ delimiter
				+ "Setiap"+ delimiter
				+ "Recurring Start Date"+ delimiter
				+ "Recurring End Date"+ delimiter
				+ "Service Code"+ delimiter
				+ "Corporate ID"+ delimiter
				+ "Corporate User ID"+ delimiter
				+ "Nomor Rekening Sumber"+ delimiter
				+ "Nama Rekening Sumber"+ delimiter
				+ "Nomor Rekening Penerima"+ delimiter
				+ "Nama Rekening Penerima"+ delimiter
				+ "Nama Alias Rekening"+ delimiter
				+ "Alamat Rekening Penerima 1"+ delimiter
				+ "Alamat Rekening Penerima 2"+ delimiter
				+ "Alamat Rekening Penerima 3"+ delimiter
				+ "Nama Bank Penerima"+ delimiter
				+ "Kode Bank Penerima"+ delimiter
				+ "Charge Instruction"+ delimiter
				+ "Mata Uang"+ delimiter
				+ "Nominal Transaksi"+ delimiter
				+ "Nominal Pokok"+ delimiter
				+ "Nominal Biaya"+ delimiter
				+ "Total Jumlah Debet"+ delimiter
				+ "Narasi"+ delimiter
				+ "Sender Reference Number"+ delimiter
				+ "Beneficiary Reference Number"+ delimiter
				+ "Is Final Payment"+ delimiter
				+ "Is Resident"+ delimiter
				+ "Kependudukan"+ delimiter
				+ "Is Citizen"+ delimiter
				+ "Kewarganegaraan"+ delimiter
				+ "Beneficiary Type"+ delimiter
				+ "Is Notify Beneficiary"+ delimiter
				+ "Email";
	}
	
	@Override
	public String toString() {
		return no + delimiter + refNo + delimiter + retrievalRefNo + delimiter + hostRefNo + delimiter + createdDt + delimiter + instructionDt + delimiter + instructionMode
				+ delimiter + status + delimiter + recurring + delimiter + reccuringEvery + delimiter + reccurringStartDt + delimiter + reccurringEndDt + delimiter
				+ srvcCd + delimiter + corpId + delimiter + userId + delimiter + sourceAcctNo + delimiter + sourceAcctNm + delimiter + benAcctNo
				+ delimiter + benAcctNm + delimiter + benAcctAlias + delimiter + benAddress1 + delimiter + benAddress2 + delimiter + benAddress3
				+ delimiter + benBankName + delimiter + benBankCd + delimiter + chargeIns + delimiter + currency + delimiter + trxAmt + delimiter
				+ debitAmt + delimiter + fee + delimiter + totalAmt + delimiter + desc + delimiter + senderRefNo + delimiter + benRefNo + delimiter
				+ isFinal + delimiter + isResident + delimiter + resident + delimiter + isCitizen + delimiter + citizen + delimiter + benType
				+ delimiter + isNotifyBen + delimiter + notifyBenValue;
	}

}
