package com.gpt.product.gpcash.corporate.workflow.model.variable;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.gpt.product.gpcash.corporate.workflow.model.CorporateVariable;

@Entity
@DiscriminatorValue(value = "s")
public class CorporateStringVariable extends CorporateVariable {
	
	@Column(name="STRING_VALUE")
	private String stringValue;

	public CorporateStringVariable() {
	}
	
	public CorporateStringVariable(String value) {
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
