package com.gpt.product.gpcash.retail.mobile;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.gpt.component.common.modularity.ModuleInfo;

@Configuration("CustomerMobileModuleConfiguration")
@PropertySource("mobile-application.properties")
public class CustomerModuleConfiguration extends ModuleInfo {
	
	public CustomerModuleConfiguration() {
		super("gp-cash-retail-mobile", new String[] { CustomerModuleConfiguration.class.getPackage().getName() });
	}
	
}
