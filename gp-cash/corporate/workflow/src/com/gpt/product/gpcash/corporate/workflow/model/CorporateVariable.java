package com.gpt.product.gpcash.corporate.workflow.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "CORP_WE_VARIABLE")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "DATA_TYPE", length = 1)
public abstract class CorporateVariable {
	
	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(name = "ID")
	private String id;
	
	@Column(name = "NAME")
	private String name;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PROCESS_INSTANCE", nullable = true)
	private CorporateProcessInstance processInstance;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TASK_INSTANCE", nullable = true)
	private CorporateTaskInstance taskInstance;
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public CorporateTaskInstance getTaskInstance() {
		return taskInstance;
	}
	
	public void setTaskInstance(CorporateTaskInstance taskInstance) {
		this.taskInstance = taskInstance;
	}
	
	public CorporateProcessInstance getProcessInstance() {
		return processInstance;
	}
	
	public void setProcessInstance(CorporateProcessInstance processInstance) {
		this.processInstance = processInstance;
	}
	
	public abstract void setValue(Object value);
	public abstract Object getValue();
}
