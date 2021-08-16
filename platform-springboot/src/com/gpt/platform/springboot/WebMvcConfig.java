package com.gpt.platform.springboot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.hazelcast.HazelcastSessionRepository;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter {

	@Autowired
	public void configureHttpSession(HazelcastSessionRepository sessionRepository, @Value("${server.session.timeout}") int maxInactiveInterval) {
		sessionRepository.setDefaultMaxInactiveInterval(maxInactiveInterval);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Bean
	public SessionRegistry sessionRegistry(HazelcastSessionRepository sessionRepository) {
		return new SpringSessionBackedSessionRegistry((FindByIndexNameSessionRepository)sessionRepository);
	}
	
}
