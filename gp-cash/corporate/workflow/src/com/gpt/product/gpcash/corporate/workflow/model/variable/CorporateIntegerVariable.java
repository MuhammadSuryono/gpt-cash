package com.gpt.product.gpcash.corporate.workflow.model.variable;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.gpt.product.gpcash.corporate.workflow.model.CorporateVariable;

@Entity
@DiscriminatorValue(value = "i")
public class CorporateIntegerVariable extends CorporateVariable {

	@Column(name="INT_VALUE")
	private Integer intValue;

	public CorporateIntegerVariable() {
	}
	
	public CorporateIntegerVariable(Integer value) {
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
