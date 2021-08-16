package com.gpt.product.gpcash.retail.transactionstatusservicemapping.model;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Cacheable
@Table(name = "CUST_TRANSACTION_STS_MAPPING")
public class CustomerTransactionStatusMappingModel implements Serializable {

	@Id
	@Column(name = "MENU_CD")
	protected String menuCode;
	
	@Column(name = "MENU_SERVICE")
	protected String menuService;

	public String getMenuCode() {
		return menuCode;
	}

	public void setMenuCode(String menuCode) {
		this.menuCode = menuCode;
	}

	public String getMenuService() {
		return menuService;
	}

	public void setMenuService(String menuService) {
		this.menuService = menuService;
	}
	
	
}
