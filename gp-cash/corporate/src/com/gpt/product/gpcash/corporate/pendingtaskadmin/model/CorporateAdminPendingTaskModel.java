package com.gpt.product.gpcash.corporate.pendingtaskadmin.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.idm.menu.model.IDMMenuModel;
import com.gpt.platform.cash.utils.Helper;

@Entity
@Table(name = "CORPADM_WF_PENDING", indexes = {
	@Index(name="CORPADM_WF_PENDING_UNIQUE1", unique = true, columnList = "REFERENCE_NO"),
	@Index(name="CORPADM_WF_PENDING_INDEX1", unique = false, columnList = "STATUS")	
})
public class CorporateAdminPendingTaskModel implements Serializable {

	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(name = "ID")
	protected String id;
	
	@Column(name = "REFERENCE_NO", nullable = false)
	protected String referenceNo;

	@Column(name = "action", nullable = false)
	protected String action;

	@Column(name = "UNIQUE_KEY", length = 1024, nullable = false)
	protected String uniqueKey;
	
	@Column(name = "UNIQUE_KEY_DISPLAY", length = 1024)
	protected String uniqueKeyDisplay;

	@Column(name = "STATUS", nullable = false)
	protected String status;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "MENU_CD", nullable = false)
	protected IDMMenuModel menu;

	@Column(name = "MODEL", nullable = false)
	protected String model;

	@Column(name = "SERVICE", nullable = false)
	protected String service;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CORP_ID", nullable = false)
	protected CorporateModel corporate;

	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(
		name = "CORPADM_WF_PENDING_VALUE", 
		joinColumns = @JoinColumn(name="PARENT_ID")
    )
	@OrderColumn(name = "IDX")
	@Column(name = "VALUES_", length = 1024)
	protected List<String> values;

	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(
		name = "CORPADM_WF_PENDING_OLD_VALUE",
		joinColumns = @JoinColumn(name="PARENT_ID")
    )
	@OrderColumn(name = "IDX")
	@Column(name = "VALUES_", length = 1024)
	protected List<String> oldValues;
	
	@Column(name="CREATED_BY")
	protected String createdBy;
	
	@Column(name="CREATED_DT")
	protected Timestamp createdDate;
	
	@Column(name="UPDATED_BY")
	protected String updatedBy;
	
	@Column(name="UPDATED_DT")
	protected Timestamp updatedDate;

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

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getUniqueKey() {
		return uniqueKey;
	}

	public void setUniqueKey(String uniqueKey) {
		this.uniqueKey = uniqueKey;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getReferenceNo() {
		return referenceNo;
	}

	public void setReferenceNo(String referenceNo) {
		this.referenceNo = referenceNo;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public List<String> getValues() {
		return values;
	}
	
	public String getValuesStr() {
		if(values != null && !values.isEmpty()) {
			return Helper.glueStringChopsBackTogether(values);
		}
		return null;
	}
	
	public void setValuesStr(String values) throws Exception {
		List<String> listOfRecord = Helper.chopItUp(values);

		if (ValueUtils.hasValue(listOfRecord)) {
			setValues(listOfRecord);
		}
	}

	public void setValues(List<String> values) {
		this.values = values;
	}
	
	public List<String> getOldValues() {
		return oldValues;
	}
	
	public String getOldValuesStr() {
		if(oldValues != null && !oldValues.isEmpty()) {
			return Helper.glueStringChopsBackTogether(oldValues);
		}
		return null;
	}

	public void setOldValues(String oldValues) throws Exception {
		List<String> listOfRecord = Helper.chopItUp(oldValues);

		if (ValueUtils.hasValue(listOfRecord)) {
			setOldValues(listOfRecord);
		}
	}

	public void setOldValues(List<String> oldValues) {
		this.oldValues = oldValues;
	}
	
	public CorporateModel getCorporate() {
		return corporate;
	}

	public void setCorporate(CorporateModel corporate) {
		this.corporate = corporate;
	}
	
	public IDMMenuModel getMenu() {
		return menu;
	}

	public void setMenu(IDMMenuModel menu) {
		this.menu = menu;
	}
	
	public String getUniqueKeyDisplay() {
		return uniqueKeyDisplay;
	}

	public void setUniqueKeyDisplay(String uniqueKeyDisplay) {
		this.uniqueKeyDisplay = uniqueKeyDisplay;
	}

	public String toString()
	  {
	    StringBuffer localStringBuffer = new StringBuffer();
	    localStringBuffer.append("PendingTaskModel");
	    localStringBuffer.append("{");
	    localStringBuffer.append("id=").append(this.id);
	    localStringBuffer.append(",uniqueKey=").append(this.uniqueKey);
	    localStringBuffer.append(",referenceNo=").append(this.referenceNo);
	    localStringBuffer.append(",action=").append(this.action);
	    localStringBuffer.append(",status=").append(this.status);
	    localStringBuffer.append(",service=").append(this.service);
	    localStringBuffer.append(",model=").append(this.model);
	    localStringBuffer.append(",createdBy=").append(this.createdBy);
	    localStringBuffer.append(",createdDate=").append(this.createdDate);
	    localStringBuffer.append('}');
	    return localStringBuffer.toString();
	  }
}
