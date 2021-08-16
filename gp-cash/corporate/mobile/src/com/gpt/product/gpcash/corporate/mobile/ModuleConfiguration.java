package com.gpt.product.gpcash.corporate.mobile;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.gpt.component.common.modularity.ModuleInfo;

@Configuration("MobileModuleConfiguration")
@PropertySource("mobile-application.properties")
public class ModuleConfiguration extends ModuleInfo {
	
	public ModuleConfiguration() {
		super("gp-cash-corporate-mobile", new String[] { ModuleConfiguration.class.getPackage().getName() });
	}
	
}
