package com.gpt.product.gpcash.retail.transaction.payee.model;

import java.io.Serializable;
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

import com.gpt.product.gpcash.biller.institution.model.InstitutionModel;
import com.gpt.product.gpcash.retail.customer.model.CustomerModel;

@Entity
@Table(name = "CUST_TRX_BILL_PAYMENT_PAYEE")
public class CustomerPayeeModel implements Serializable {
		// START Basic Information
		@Id
		@GeneratedValue(generator = "uuid")
		@GenericGenerator(name = "uuid", strategy = "uuid2")
		@Column(name = "ID")
		protected String id;
		
		@Version
		protected int version;
		
		@Column(name = "PAYEE_NM")
		protected String payeeName;
		
		@Column(name = "DSCP")
		protected String description;
		
		@Column(name = "VALUE_1")
		protected String value1;
		
		@Column(name = "VALUE_2")
		protected String value2;
		
		@Column(name = "VALUE_3")
		protected String value3;
		
		@Column(name = "VALUE_4")
		protected String value4;
		
		@Column(name = "VALUE_5")
		protected String value5;
		
		@ManyToOne(fetch = FetchType.LAZY)
		@JoinColumn(name = "CUST_ID", nullable = false)
		protected CustomerModel customer;
		
		@ManyToOne(fetch = FetchType.LAZY)
		@JoinColumn(name = "INSTITUTION_CD")
		protected InstitutionModel institution;
		
		// START Default Information
		@Column(name = "CREATED_BY")
		protected String createdBy;
		
		@Column(name = "CREATED_DT")
		protected Timestamp createdDate;

		@Column(name = "UPDATED_BY")
		protected String updatedBy;

		@Column(name = "UPDATED_DT")
		protected Timestamp updatedDate;

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

		public String getValue1() {
			return value1;
		}

		public void setValue1(String value1) {
			this.value1 = value1;
		}

		public String getValue2() {
			return value2;
		}

		public void setValue2(String value2) {
			this.value2 = value2;
		}

		public String getValue3() {
			return value3;
		}

		public void setValue3(String value3) {
			this.value3 = value3;
		}

		public String getValue4() {
			return value4;
		}

		public void setValue4(String value4) {
			this.value4 = value4;
		}

		public String getValue5() {
			return value5;
		}

		public void setValue5(String value5) {
			this.value5 = value5;
		}

		public CustomerModel getCustomer() {
			return customer;
		}

		public void setCustomer(CustomerModel customer) {
			this.customer = customer;
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

		public String getPayeeName() {
			return payeeName;
		}

		public void setPayeeName(String payeeName) {
			this.payeeName = payeeName;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public InstitutionModel getInstitution() {
			return institution;
		}

		public void setInstitution(InstitutionModel institution) {
			this.institution = institution;
		}
}