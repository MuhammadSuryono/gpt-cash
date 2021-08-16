package com.gpt.product.gpcash;

import org.springframework.context.annotation.Configuration;

import com.gpt.component.common.modularity.ModuleInfo;

@Configuration("GPCashModuleConfiguration")
public class ModuleConfiguration extends ModuleInfo {
	
	public ModuleConfiguration() {
		super("gp-cash", new String[] { ModuleConfiguration.class.getPackage().getName() });
	}
	
}
