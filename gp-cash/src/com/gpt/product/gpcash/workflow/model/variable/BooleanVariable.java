package com.gpt.product.gpcash.workflow.model.variable;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.gpt.product.gpcash.workflow.model.Variable;

@Entity
@DiscriminatorValue(value = "o")
public class BooleanVariable extends Variable {

	@Column(name="BOOLEAN_VALUE")
	private Boolean booleanValue;

	public BooleanVariable() {
	}
	
	public BooleanVariable(Boolean value) {
		this.booleanValue = value;
	}

	public Boolean getBooleanValue() {
		return booleanValue;
	}
	
	public void setBooleanValue(Boolean booleanValue) {
		this.booleanValue = booleanValue;
	}

	@Override
	public void setValue(Object value) {
		setBooleanValue((Boolean)value);
	}

	@Override
	public Object getValue() {
		return getBooleanValue();
	}
}
