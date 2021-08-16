package com.gpt.product.gpcash.corporate;

import org.springframework.context.annotation.Configuration;

import com.gpt.component.common.modularity.ModuleInfo;

@Configuration("CorpModuleConfiguration")
public class ModuleConfiguration extends ModuleInfo {
	
	public ModuleConfiguration() {
		super("gp-cash-corporate", new String[] { ModuleConfiguration.class.getPackage().getName() });
	}
	
}
