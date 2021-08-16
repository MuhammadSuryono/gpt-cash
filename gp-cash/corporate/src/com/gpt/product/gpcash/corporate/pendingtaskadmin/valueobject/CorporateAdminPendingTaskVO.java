package com.gpt.product.gpcash.corporate.pendingtaskadmin.valueobject;

import java.sql.Timestamp;

public class CorporateAdminPendingTaskVO {
	protected String id;
	protected String corporateId;
	protected String uniqueKey;
	protected String uniqueKeyDisplay;
	protected String referenceNo;
	protected String action;
	protected String createdBy;
	protected String menuCode;
	protected Object jsonObject;
	protected Object jsonObjectOld;
	protected String status;
	protected String service;
	protected Timestamp createdDate;
	protected String model;
	protected String actionBy;
	
	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public Timestamp getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Timestamp createdDate) {
		this.createdDate = createdDate;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getStatus() {
		return status;
	}

	public String getReferenceNo() {
		return referenceNo;
	}

	public void setReferenceNo(String referenceNo) {
		this.referenceNo = referenceNo;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUniqueKey() {
		return uniqueKey;
	}

	public void setUniqueKey(String uniqueKey) {
		this.uniqueKey = uniqueKey;
	}

	public String getMenuCode() {
		return menuCode;
	}

	public void setMenuCode(String menuCode) {
		this.menuCode = menuCode;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Object getJsonObject() {
		return jsonObject;
	}

	public void setJsonObject(Object jsonObject) {
		this.jsonObject = jsonObject;
	}

	public Object getJsonObjectOld() {
		return jsonObjectOld;
	}

	public void setJsonObjectOld(Object jsonObjectOld) {
		this.jsonObjectOld = jsonObjectOld;
	}
	
	
	
	public String getCorporateId() {
		return corporateId;
	}

	public void setCorporateId(String corporateId) {
		this.corporateId = corporateId;
	}
	
	public String getUniqueKeyDisplay() {
		return uniqueKeyDisplay;
	}

	public void setUniqueKeyDisplay(String uniqueKeyDisplay) {
		this.uniqueKeyDisplay = uniqueKeyDisplay;
	}
	
	

	public String getActionBy() {
		return actionBy;
	}

	public void setActionBy(String actionBy) {
		this.actionBy = actionBy;
	}

	public String toString()
	  {
	    StringBuffer localStringBuffer = new StringBuffer();
	    localStringBuffer.append("PendingTaskVO");
	    localStringBuffer.append("{");
	    localStringBuffer.append("id=").append(this.id);
	    localStringBuffer.append(",corporateId=").append(this.corporateId);
	    localStringBuffer.append(",uniqueKey=").append(this.uniqueKey);
	    localStringBuffer.append(",referenceNo=").append(this.referenceNo);
	    localStringBuffer.append(",action=").append(this.action);
	    localStringBuffer.append(",menuCode=").append(this.menuCode);
	    localStringBuffer.append(",status=").append(this.status);
	    localStringBuffer.append(",service=").append(this.service);
	    localStringBuffer.append(",model=").append(this.model);
	    localStringBuffer.append(",createdBy=").append(this.createdBy);
	    localStringBuffer.append(",createdDate=").append(this.createdDate);
	    localStringBuffer.append(",jsonObject=").append(this.jsonObject);
	    localStringBuffer.append(",jsonObjectOld=").append(this.jsonObjectOld);
	    localStringBuffer.append('}');
	    return localStringBuffer.toString();
	  }
}
