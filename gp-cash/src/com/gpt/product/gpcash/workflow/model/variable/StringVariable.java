package com.gpt.product.gpcash.workflow.model.variable;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.gpt.product.gpcash.workflow.model.Variable;

@Entity
@DiscriminatorValue(value = "s")
public class StringVariable extends Variable {
	
	@Column(name="STRING_VALUE")
	private String stringValue;

	public StringVariable() {
	}
	
	public StringVariable(String value) {
		this.stringValue = value;
	}
	
	public String getStringValue() {
		return stringValue;
	}
	
	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}
	
	@Override
	public void setValue(Object value) {
		setStringValue((String)value);
	}
	
	@Override
	public Object getValue() {
		return getStringValue();
	}
}
