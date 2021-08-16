package com.gpt.platform.springboot.session;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.session.MapSession;

import com.hazelcast.query.extractor.ValueCollector;
import com.hazelcast.query.extractor.ValueExtractor;

public class PrincipalNameExtractor extends ValueExtractor<MapSession, String> {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void extract(MapSession target, String argument, ValueCollector collector) {
		SecurityContext authentication = target.getAttribute("SPRING_SECURITY_CONTEXT");
		if (authentication != null) {
			String name = authentication.getAuthentication().getName();
			if(name != null)
				collector.addObject(name);
		}
	}

}
