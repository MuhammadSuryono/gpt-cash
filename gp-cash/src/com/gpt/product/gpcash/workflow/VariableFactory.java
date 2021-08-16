package com.gpt.product.gpcash.workflow;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.gpt.product.gpcash.workflow.model.Variable;
import com.gpt.product.gpcash.workflow.model.variable.BigDecimalVariable;
import com.gpt.product.gpcash.workflow.model.variable.BooleanVariable;
import com.gpt.product.gpcash.workflow.model.variable.IntegerVariable;
import com.gpt.product.gpcash.workflow.model.variable.StringVariable;
import com.gpt.product.gpcash.workflow.model.variable.TimestampVariable;

@Component
public class VariableFactory {

	@SuppressWarnings("rawtypes")
	protected static Map<Class, IVariableFactory> variableFactories;
	
	static {
		variableFactories = new HashMap<>();
		variableFactories.put(String.class, v -> { return new StringVariable((String)v); });
		variableFactories.put(Boolean.class, v -> { return new BooleanVariable((Boolean)v); });
		variableFactories.put(Integer.class, v -> { return new IntegerVariable((Integer)v); });
		variableFactories.put(Timestamp.class, v -> { return new TimestampVariable((Timestamp)v); });
		variableFactories.put(BigDecimal.class, v -> { return new BigDecimalVariable((BigDecimal)v); });
	}

	interface IVariableFactory<T> {
		Variable createVariable(T value);
	}
	
	@SuppressWarnings("unchecked")
	public Variable createVariable(String name, Object value) {
		Variable var = variableFactories.get(value.getClass()).createVariable(value);		
		var.setName(name);
		return var;
	}
}
