package com.gpt.product.gpcash.corporate.usertransactionmapping.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "CORP_USR_TRX_MAPPING")
public class UserTransactionMappingModel {
	@Id
	@Column(name = "ID")
	protected String id;
	
	@Column(name="TOTAL_CREATED_TRX")
	protected int totalCreatedTransaction;
	
	@Column(name="TOTAL_EXECUTED_TRX")
	protected int totalExecutedTransaction;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getTotalCreatedTransaction() {
		return totalCreatedTransaction;
	}

	public void setTotalCreatedTransaction(int totalCreatedTransaction) {
		this.totalCreatedTransaction = totalCreatedTransaction;
	}

	public int getTotalExecutedTransaction() {
		return totalExecutedTransaction;
	}

	public void setTotalExecutedTransaction(int totalExecutedTransaction) {
		this.totalExecutedTransaction = totalExecutedTransaction;
	}
	
	
}
