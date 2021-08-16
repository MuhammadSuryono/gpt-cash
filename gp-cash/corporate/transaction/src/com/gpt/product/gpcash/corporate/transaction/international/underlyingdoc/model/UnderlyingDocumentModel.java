package com.gpt.product.gpcash.corporate.transaction.international.underlyingdoc.model;

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

import com.gpt.component.maintenance.branch.model.BranchModel;
import com.gpt.component.maintenance.parametermt.model.DocumentTypeModel;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;

@Entity
@Table(name = "TRX_INTERNATIONAL_DOC")
public class UnderlyingDocumentModel implements Serializable {
	
		@Id
		@GeneratedValue(generator = "uuid")
		@GenericGenerator(name = "uuid", strategy = "uuid2")
		@Column(name = "ID")
		protected String id;
		
		@Version
		protected int version;

		@Column(name = "UNDERLYING_AMT", precision=25, scale=7)
		protected BigDecimal underlyingAmount;

		@Column(name = "UNDERLYING_CCY_CD")
		protected String underlyingCurrency;

		@Column(name = "REMARK1")
		protected String remark1;
		
		@Column(name = "UNDERLYING_CD")
		protected String underlyingCode;
		
		@ManyToOne(fetch = FetchType.LAZY)
		@JoinColumn(name = "DOCUMENT_TYP_CD", nullable = false)
		protected DocumentTypeModel documentType;
		
		@ManyToOne(fetch = FetchType.LAZY)
		@JoinColumn(name = "CORP_ID", nullable = false)
		protected CorporateModel corporate;

		@ManyToOne(fetch = FetchType.LAZY)
		@JoinColumn(name = "BRANCH_CD", nullable = false)
		protected BranchModel branch;
		
		@Column(name = "EXPIRY_DT")
		protected Timestamp expiryDate;

		@Column(name = "CREATED_BY")
		protected String createdBy;
		
		@Column(name = "CREATED_DT")
		protected Timestamp createdDate;

		@Column(name = "UPDATED_BY")
		protected String updatedBy;

		@Column(name = "UPDATED_DT")
		protected Timestamp updatedDate;
		
		@Column(name = "REF_NO")
		protected String referenceNo;

		@Column(name = "DELETE_REF_NO")
		protected String deleteReferenceNo;
		
		@Column(name = "IS_DELETE")
		protected String deleteFlag;

		public int getVersion() {
			return version;
		}

		public void setVersion(int version) {
			this.version = version;
		}

		public String getRemark1() {
			return remark1;
		}

		public void setRemark1(String remark1) {
			this.remark1 = remark1;
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

		public String getCreatedBy() {
			return createdBy;
		}

		public void setCreatedBy(String createdBy) {
			this.createdBy = createdBy;
		}

		public CorporateModel getCorporate() {
			return corporate;
		}

		public void setCorporate(CorporateModel corporate) {
			this.corporate = corporate;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public BigDecimal getUnderlyingAmount() {
			return underlyingAmount;
		}

		public void setUnderlyingAmount(BigDecimal underlyingAmount) {
			this.underlyingAmount = underlyingAmount;
		}

		public String getUnderlyingCurrency() {
			return underlyingCurrency;
		}

		public void setUnderlyingCurrency(String underlyingCurrency) {
			this.underlyingCurrency = underlyingCurrency;
		}

		public String getUnderlyingCode() {
			return underlyingCode;
		}

		public void setUnderlyingCode(String underlyingCode) {
			this.underlyingCode = underlyingCode;
		}

		public DocumentTypeModel getDocumentType() {
			return documentType;
		}

		public void setDocumentType(DocumentTypeModel documentType) {
			this.documentType = documentType;
		}

		public BranchModel getBranch() {
			return branch;
		}

		public void setBranch(BranchModel branch) {
			this.branch = branch;
		}

		public Timestamp getExpiryDate() {
			return expiryDate;
		}

		public void setExpiryDate(Timestamp expiryDate) {
			this.expiryDate = expiryDate;
		}

		public String getReferenceNo() {
			return referenceNo;
		}

		public void setReferenceNo(String referenceNo) {
			this.referenceNo = referenceNo;
		}

		public String getDeleteReferenceNo() {
			return deleteReferenceNo;
		}

		public void setDeleteReferenceNo(String deleteReferenceNo) {
			this.deleteReferenceNo = deleteReferenceNo;
		}

		public String getDeleteFlag() {
			return deleteFlag;
		}

		public void setDeleteFlag(String deleteFlag) {
			this.deleteFlag = deleteFlag;
		}		
		
}
