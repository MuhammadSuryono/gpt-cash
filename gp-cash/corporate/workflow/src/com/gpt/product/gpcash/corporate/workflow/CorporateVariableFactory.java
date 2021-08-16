package com.gpt.product.gpcash.corporate.workflow;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.gpt.product.gpcash.corporate.workflow.model.CorporateVariable;
import com.gpt.product.gpcash.corporate.workflow.model.variable.CorporateBigDecimalVariable;
import com.gpt.product.gpcash.corporate.workflow.model.variable.CorporateBooleanVariable;
import com.gpt.product.gpcash.corporate.workflow.model.variable.CorporateIntegerVariable;
import com.gpt.product.gpcash.corporate.workflow.model.variable.CorporateStringVariable;
import com.gpt.product.gpcash.corporate.workflow.model.variable.CorporateTimestampVariable;

@Component
public class CorporateVariableFactory {

	@SuppressWarnings("rawtypes")
	protected static Map<Class, IVariableFactory> variableFactories;
	
	static {
		variableFactories = new HashMap<>();
		variableFactories.put(String.class, v -> { return new CorporateStringVariable((String)v); });
		variableFactories.put(Boolean.class, v -> { return new CorporateBooleanVariable((Boolean)v); });
		variableFactories.put(Integer.class, v -> { return new CorporateIntegerVariable((Integer)v); });
		variableFactories.put(Timestamp.class, v -> { return new CorporateTimestampVariable((Timestamp)v); });
		variableFactories.put(BigDecimal.class, v -> { return new CorporateBigDecimalVariable((BigDecimal)v); });
	}

	interface IVariableFactory<T> {
		CorporateVariable createVariable(T value);
	}
	
	@SuppressWarnings("unchecked")
	public CorporateVariable createVariable(String name, Object value) {
		CorporateVariable var = variableFactories.get(value.getClass()).createVariable(value);		
		var.setName(name);
		return var;
	}
}
