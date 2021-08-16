package com.gpt.component.serializer;

import org.nustaq.serialization.FSTConfiguration;

public class CustomizedFSTObjectUnsafeSerializer extends CustomizedFSTObjectSerializer {

	@Override
	protected void createConfiguration() {
		conf = FSTConfiguration.createUnsafeBinaryConfiguration();
	}
	
}
