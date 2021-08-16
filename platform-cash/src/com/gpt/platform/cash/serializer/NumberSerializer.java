package com.gpt.platform.cash.serializer;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

@Component
public class NumberSerializer extends JsonSerializer<Number> {

	@Override
	public void serialize(Number value, JsonGenerator jgen, SerializerProvider provider)
			throws IOException, JsonProcessingException {
		jgen.writeString(value.toString());
	}

	@Override
	public Class<Number> handledType() {
		return Number.class;
	}
	
}
