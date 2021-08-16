package com.gpt.product.gpcash.corporate.transaction;

import org.springframework.context.annotation.Configuration;

import com.gpt.component.common.modularity.ModuleInfo;

@Configuration("CorpTrxModuleConfiguration")
public class ModuleConfiguration extends ModuleInfo {
	
	public ModuleConfiguration() {
		super("gp-cash-corporate-transaction", new String[] { ModuleConfiguration.class.getPackage().getName() });
	}
	
}
