package com.gpt.platform.springboot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.gpt.platform.springboot.csrf.HttpSessionCsrfTokenRepository;

@Configuration
@Order(50)
public class GlobalWebSecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Value("${management.context-path}")
	private String managementContextPath;
	
	@Override
    protected void configure(HttpSecurity http) throws Exception {
		http.antMatcher(managementContextPath + "/**")
			.csrf().disable()
			.authorizeRequests()
			.antMatchers(managementContextPath + "/hystrix.stream").permitAll()
			.anyRequest()
			.authenticated()
			.and()
			.httpBasic();
	}
	
	@Bean
	public CsrfTokenRepository csrfTokenRepository() {
	    return new HttpSessionCsrfTokenRepository(); 
	}	
	
	@Bean
	public CorsConfigurationSource corsConfigurationSource(@Value("${spring.security.cors.allowed-origins}") String strAllowedOrigins) {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true);

		String[] allowedOrigins = strAllowedOrigins.split(",");
		for(String allowedOrigin : allowedOrigins) {
			config.addAllowedOrigin(allowedOrigin.trim());
		}
		
		config.addAllowedHeader("Origin");
		config.addAllowedHeader("Content-Type");
		config.addAllowedHeader("Accept");
		config.addAllowedHeader("X-Requested-With");
		config.addAllowedHeader("X-XSRF-TOKEN");
		
		config.setMaxAge(3600L);
		config.addAllowedMethod("POST");
		config.addAllowedMethod("GET");
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}
}
