package com.gpt.product.gpcash.workflow.model.variable;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.gpt.product.gpcash.workflow.model.Variable;

@Entity
@DiscriminatorValue(value = "i")
public class IntegerVariable extends Variable {

	@Column(name="INT_VALUE")
	private Integer intValue;

	public IntegerVariable() {
	}
	
	public IntegerVariable(Integer value) {
		this.intValue = value;
	}
	
	public Integer getIntValue() {
		return intValue;
	}
	
	public void setIntValue(Integer intValue) {
		this.intValue = intValue;
	}

	@Override
	public void setValue(Object value) {
		setIntValue((Integer)value);
	}

	@Override
	public Object getValue() {
		return getIntValue();
	}
}
