package com.gpt.platform.springboot;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ImportResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


import com.gpt.component.common.spring.Util;

@SpringBootApplication(scanBasePackages = {"com.gpt"})
@EntityScan(basePackages = {"com.gpt"})
@EnableJpaRepositories(basePackages = {"com.gpt"})
@ImportResource({"classpath*:/META-INF/configs/appCtx-*.xml", "classpath*:appCtx-*.xml"})
public class Main extends SpringBootServletInitializer {
	
	public static void main(String[] args) {
		new SpringApplicationBuilder(Main.class).beanNameGenerator(Util::generateBeanName).run(args);
	}
	
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(Main.class).resourceLoader(new ResourceResolver()).beanNameGenerator(Util::generateBeanName);
	}
	
}
