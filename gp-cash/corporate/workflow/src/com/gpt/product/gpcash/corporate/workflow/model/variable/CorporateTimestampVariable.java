package com.gpt.product.gpcash.corporate.workflow.model.variable;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.gpt.product.gpcash.corporate.workflow.model.CorporateVariable;

@Entity
@DiscriminatorValue(value = "t")
public class CorporateTimestampVariable extends CorporateVariable {
		
	@Column(name="TIMESTAMP_VALUE")
	private Timestamp timestampVariable;
	
	public CorporateTimestampVariable() {
	}
	
	public CorporateTimestampVariable(Timestamp value) {
		timestampVariable = value;
	}
	
	public Timestamp getTimestampVariable() {
		return timestampVariable;
	}
	
	public void setTimestampVariable(Timestamp timestampVariable) {
		this.timestampVariable = timestampVariable;
	}

	@Override
	public void setValue(Object value) {
		setTimestampVariable((Timestamp)value);
	}

	@Override
	public Object getValue() {
		return getTimestampVariable();
	}
}
