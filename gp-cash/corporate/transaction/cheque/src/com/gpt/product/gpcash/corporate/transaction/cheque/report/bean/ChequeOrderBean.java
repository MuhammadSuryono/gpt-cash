package com.gpt.product.gpcash.corporate.transaction.cheque.report.bean;

public class ChequeOrderBean {
	private String no;
	private String orderNo;
	private String orderDateTime;
	private String requestBy;
	private String pickupSchedule;
	private String status;
	private String fileFormat;
	public String getNo() {
		return no;
	}
	public void setNo(String no) {
		this.no = no;
	}
	public String getOrderNo() {
		return orderNo;
	}
	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}
	public String getOrderDateTime() {
		return orderDateTime;
	}
	public void setOrderDateTime(String orderDateTime) {
		this.orderDateTime = orderDateTime;
	}
	public String getRequestBy() {
		return requestBy;
	}
	public void setRequestBy(String requestBy) {
		this.requestBy = requestBy;
	}
	public String getPickupSchedule() {
		return pickupSchedule;
	}
	public void setPickupSchedule(String pickupSchedule) {
		this.pickupSchedule = pickupSchedule;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getFileFormat() {
		return fileFormat;
	}
	public void setFileFormat(String fileFormat) {
		this.fileFormat = fileFormat;
	}
	
	
}
