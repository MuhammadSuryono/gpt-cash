package com.gpt.product.gpcash.workflow.model.variable;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.gpt.product.gpcash.workflow.model.Variable;

@Entity
@DiscriminatorValue(value = "d")
public class BigDecimalVariable extends Variable {
	
	@Column(name="BIG_DECIMAL_VALUE", precision=25, scale=7)
	private BigDecimal bigDecimalValue;

	public BigDecimalVariable() {
	}
	
	public BigDecimalVariable(BigDecimal value) {
		this.bigDecimalValue = value;
	}
	
	public BigDecimal getBigDecimalValue() {
		return bigDecimalValue;
	}
	
	public void setBigDecimalValue(BigDecimal bigDecimalValue) {
		this.bigDecimalValue = bigDecimalValue;
	}

	@Override
	public void setValue(Object value) {
		setBigDecimalValue((BigDecimal)value);
	}

	@Override
	public Object getValue() {
		return getBigDecimalValue();
	}
}
