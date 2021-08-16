package com.gpt.product.gpcash.servicecurrencymatrix.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.GenericGenerator;

import com.gpt.component.idm.app.model.IDMApplicationModel;
import com.gpt.product.gpcash.currencymatrix.model.CurrencyMatrixModel;
import com.gpt.product.gpcash.service.model.ServiceModel;

@Entity
@Cacheable
@Table(name = "PRO_SRVC_CCY_MTRX")
public class ServiceCurrencyMatrixModel implements Serializable {
	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(name = "ID")
	protected String id;

	@Version
	protected int version;

	@ManyToOne
	@JoinColumn(name = "SRVC_CD", nullable = false)
	protected ServiceModel service;
	
	@ManyToOne
	@JoinColumn(name = "CCY_MTRX_CD", nullable = false)
	protected CurrencyMatrixModel currencyMatrix;
	
	@ManyToOne
	@JoinColumn(name = "APP_CD", nullable = false)
	protected IDMApplicationModel application;
	
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

	public ServiceModel getService() {
		return service;
	}

	public void setService(ServiceModel service) {
		this.service = service;
	}

	public CurrencyMatrixModel getCurrencyMatrix() {
		return currencyMatrix;
	}

	public void setCurrencyMatrix(CurrencyMatrixModel currencyMatrix) {
		this.currencyMatrix = currencyMatrix;
	}

	public IDMApplicationModel getApplication() {
		return application;
	}

	public void setApplication(IDMApplicationModel application) {
		this.application = application;
	}
	
}
