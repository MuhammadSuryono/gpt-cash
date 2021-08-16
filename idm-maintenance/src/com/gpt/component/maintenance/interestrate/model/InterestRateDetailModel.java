package com.gpt.component.maintenance.interestrate.model;

import java.io.Serializable;

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

@Entity
@Table(name = "MT_INTEREST_RT_DTL")
public class InterestRateDetailModel implements Serializable {
	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(name = "ID")
	protected String id;

	@Version
	protected int version;

	@Column(name = "IDX")
	protected int idx;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "INTEREST_RT_ID", nullable = false)
	protected InterestRateModel interest;
	
	@Column(name = "BALANCE")
	protected String balance;
	
	@Column(name = "PERIOD")
	protected String period;
	
	@Column(name = "INTEREST_RT")
	protected String interestRate;
	
	@Column(name = "IS_DELETE")
	protected String deleteFlag;

	public String getBalance() {
		return balance;
	}

	public void setBalance(String balance) {
		this.balance = balance;
	}

	public String getPeriod() {
		return period;
	}

	public void setPeriod(String period) {
		this.period = period;
	}

	public String getInterestRate() {
		return interestRate;
	}

	public void setInterestRate(String interestRate) {
		this.interestRate = interestRate;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getIdx() {
		return idx;
	}

	public void setIdx(int idx) {
		this.idx = idx;
	}

	public String getDeleteFlag() {
		return deleteFlag;
	}

	public void setDeleteFlag(String deleteFlag) {
		this.deleteFlag = deleteFlag;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public InterestRateModel getInterest() {
		return interest;
	}

	public void setInterest(InterestRateModel interest) {
		this.interest = interest;
	}
}
