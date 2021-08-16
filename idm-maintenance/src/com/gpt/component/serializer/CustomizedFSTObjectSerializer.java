package com.gpt.component.serializer;

import com.gpt.component.common.serializer.FSTObjectSerializer;

public class CustomizedFSTObjectSerializer extends FSTObjectSerializer {

	@Override
	protected void registerClasses() {
		super.registerClasses();
		try {conf.registerClass(Class.forName("org.springframework.session.MapSession"));}catch(Exception e) {}
		try {conf.registerClass(Class.forName("org.springframework.security.core.authority.SimpleGrantedAuthority"));}catch(Exception e) {}
		try {conf.registerClass(Class.forName("com.gpt.component.idm.web.security.IDMAuthentication"));}catch(Exception e) {}
	}
	
}
