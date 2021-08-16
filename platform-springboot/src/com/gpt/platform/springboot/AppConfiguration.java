package com.gpt.platform.springboot;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.gpt.component.common.Constants;
import com.gpt.component.common.broadcast.Broadcaster;
import com.gpt.component.common.broadcast.HazelcastBroadcaster;
import com.gpt.component.common.configuration.AutoDiscoveryConfiguration;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spring.cache.HazelcastCacheManager;

/**
 * This is application level configurations, replacing xml based configurations
 *
 */
@Configuration
@EnableCaching
public class AppConfiguration extends AutoDiscoveryConfiguration {

	@Value("${gpcash.circuit-breaker.enable}")
	private boolean enableCircuitBreaker;
	
	@Override
	protected boolean enableCircuitBreakerForExternalService() {
		return enableCircuitBreaker;
	}
	
	@Bean("Broadcaster")
	public Broadcaster createBroadcaster(HazelcastInstance hzInstance) {
		return new HazelcastBroadcaster(hzInstance);
	}
	
	@Bean
    public CacheManager cacheManager(HazelcastInstance hzInstance) {
        return new HazelcastCacheManager(hzInstance);
    }
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Bean
	public MappingJackson2HttpMessageConverter standardMessageConverter(@Autowired(required=false) List<JsonSerializer> serializers) {
		MappingJackson2HttpMessageConverter mapper = new MappingJackson2HttpMessageConverter();
		com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
		objectMapper.setDateFormat(new SimpleDateFormat(Constants.DATE_TIME_FORMAT));

		if(serializers != null && !serializers.isEmpty()) {
			SimpleModule module = new SimpleModule();
			for(JsonSerializer serializer : serializers) {
				module.addSerializer(serializer.handledType(), serializer);
				objectMapper.registerModule(module);
			}
		}
		
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.setObjectMapper(objectMapper);
		return mapper;
	}
	
	@Bean
	public ObjectMapper standardObjectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setDateFormat(new SimpleDateFormat(Constants.DATE_TIME_FORMAT));
		return objectMapper;
	}
	
	@Bean
	public LocaleResolver localeResolver() {
		CookieLocaleResolver r = new CookieLocaleResolver();
		r.setDefaultLocale(new Locale("in"));
		return r;
	}

//	@Bean
//	public SessionLocaleResolver localeResolver() {
//		SessionLocaleResolver r = new SessionLocaleResolver();
//		r.setDefaultLocale(new Locale("in"));
//		return r;
//	}

}
