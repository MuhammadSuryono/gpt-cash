package com.gpt.product.gpcash.corporate.inquiry;

import org.springframework.context.annotation.Configuration;

import com.gpt.component.common.modularity.ModuleInfo;

@Configuration("CorpInquiryModuleConfiguration")
public class ModuleConfiguration extends ModuleInfo {
	
	public ModuleConfiguration() {
		super("gp-cash-corporate-inquiry", new String[] { ModuleConfiguration.class.getPackage().getName() });
	}
	
}
