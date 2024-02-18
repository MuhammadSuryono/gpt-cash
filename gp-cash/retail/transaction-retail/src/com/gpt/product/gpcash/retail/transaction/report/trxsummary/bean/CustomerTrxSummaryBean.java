package com.gpt.product.gpcash.retail.transaction.report.trxsummary.bean;

public class CustomerTrxSummaryBean {
	private String no;
	private String service;
	private String success;
	private String successCurrency;
	private String successAmount;
	
	private String failed;
	private String failedCurrency;
	private String failedAmount;
	
	private String totalTransaction;
	
	private String fileFormat;

	public String getNo() {
		return no;
	}

	public void setNo(String no) {
		this.no = no;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getSuccess() {
		return success;
	}

	public void setSuccess(String success) {
		this.success = success;
	}

	public String getSuccessCurrency() {
		return successCurrency;
	}

	public void setSuccessCurrency(String successCurrency) {
		this.successCurrency = successCurrency;
	}

	public String getSuccessAmount() {
		return successAmount;
	}

	public void setSuccessAmount(String successAmount) {
		this.successAmount = successAmount;
	}

	public String getFailed() {
		return failed;
	}

	public void setFailed(String failed) {
		this.failed = failed;
	}

	public String getFailedCurrency() {
		return failedCurrency;
	}

	public void setFailedCurrency(String failedCurrency) {
		this.failedCurrency = failedCurrency;
	}

	public String getFailedAmount() {
		return failedAmount;
	}

	public void setFailedAmount(String failedAmount) {
		this.failedAmount = failedAmount;
	}

	public String getTotalTransaction() {
		return totalTransaction;
	}

	public void setTotalTransaction(String totalTransaction) {
		this.totalTransaction = totalTransaction;
	}

	public String getFileFormat() {
		return fileFormat;
	}

	public void setFileFormat(String fileFormat) {
		this.fileFormat = fileFormat;
	}
	
	
}
