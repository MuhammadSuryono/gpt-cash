package com.gpt.component.maintenance.promo.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Cacheable
@Table(name = "MT_PROMO")
public class PromoModel implements Serializable {
	/**
	 * MUST use protected for parameter maintenance model if not got error using clazz.getDeclaredField in
	 * ParameterMaintenanceServiceImpl 
	 */
	
	@Id
	@Column(name = "CD")
	public String code;

	@Column(name = "NM")
	public String name;
	
	@Column(name = "DSCP")
	public String dscp;

	@Column(name = "IS_DELETE")
	public String deleteFlag;
	
	@Column(name="CREATED_BY")
	public String createdBy;
	
	@Column(name="CREATED_DT")
	public Timestamp createdDate;
	
	@Column(name="UPDATED_BY")
	public String updatedBy;
	
	@Column(name="UPDATED_DT")
	public Timestamp updatedDate;
	
	@Column(name = "CORP_ID")
	public String corpId;
	
	@Column(name = "IMG_URL")
	protected String promoImgUrl;
	
	@Column(name = "IMG_FILE_NAME")
	protected String promoImgFileName;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDscp() {
		return dscp;
	}

	public void setDscp(String dscp) {
		this.dscp = dscp;
	}

	public String getDeleteFlag() {
		return deleteFlag;
	}

	public void setDeleteFlag(String deleteFlag) {
		this.deleteFlag = deleteFlag;
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

	public String getCorpId() {
		return corpId;
	}

	public void setCorpId(String corpId) {
		this.corpId = corpId;
	}

	public String getPromoImgUrl() {
		return promoImgUrl;
	}

	public void setPromoImgUrl(String promoImgUrl) {
		this.promoImgUrl = promoImgUrl;
	}

	public String getPromoImgFileName() {
		return promoImgFileName;
	}

	public void setPromoImgFileName(String promoImgFileName) {
		this.promoImgFileName = promoImgFileName;
	}
	
}
