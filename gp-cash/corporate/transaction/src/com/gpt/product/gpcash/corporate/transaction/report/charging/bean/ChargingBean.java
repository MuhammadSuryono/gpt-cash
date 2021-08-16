package com.gpt.product.gpcash.corporate.transaction.report.charging.bean;

public class ChargingBean {
	private String no;
	private String service;
	private String corporate;
	private String volume;
	private String currency;
	private String amount;
	private String fileFormat;
	
	private String totalCorporate;
	private String totalVolume;
	private String totalFeeAmount;
	
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
	public String getCorporate() {
		return corporate;
	}
	public void setCorporate(String corporate) {
		this.corporate = corporate;
	}
	public String getVolume() {
		return volume;
	}
	public void setVolume(String volume) {
		this.volume = volume;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getFileFormat() {
		return fileFormat;
	}
	public void setFileFormat(String fileFormat) {
		this.fileFormat = fileFormat;
	}
	public String getTotalCorporate() {
		return totalCorporate;
	}
	public void setTotalCorporate(String totalCorporate) {
		this.totalCorporate = totalCorporate;
	}
	public String getTotalVolume() {
		return totalVolume;
	}
	public void setTotalVolume(String totalVolume) {
		this.totalVolume = totalVolume;
	}
	public String getTotalFeeAmount() {
		return totalFeeAmount;
	}
	public void setTotalFeeAmount(String totalFeeAmount) {
		this.totalFeeAmount = totalFeeAmount;
	}
	
}
