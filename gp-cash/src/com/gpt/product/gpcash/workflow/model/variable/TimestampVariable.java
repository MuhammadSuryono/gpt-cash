package com.gpt.product.gpcash.workflow.model.variable;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.gpt.product.gpcash.workflow.model.Variable;

@Entity
@DiscriminatorValue(value = "t")
public class TimestampVariable extends Variable {
		
	@Column(name="TIMESTAMP_VALUE")
	private Timestamp timestampVariable;
	
	public TimestampVariable() {
	}
	
	public TimestampVariable(Timestamp value) {
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
