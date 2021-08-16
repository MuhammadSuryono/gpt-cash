package com.gpt.product.gpcash.retail.transaction;

import org.springframework.context.annotation.Configuration;

import com.gpt.component.common.modularity.ModuleInfo;

@Configuration("CustTrxModuleConfiguration")
public class ModuleConfiguration extends ModuleInfo {
	
	public ModuleConfiguration() {
		super("gp-cash-retail-transaction", new String[] { ModuleConfiguration.class.getPackage().getName() });
	}
	
}
