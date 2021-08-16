package com.gpt.product.gpcash.workflow.model;

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
@Table(name = "WE_VARIABLE")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "DATA_TYPE", length = 1)
public abstract class Variable {
	
	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(name = "ID")
	private String id;
	
	@Column(name = "NAME")
	private String name;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PROCESS_INSTANCE", nullable = true)
	private ProcessInstance processInstance;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TASK_INSTANCE", nullable = true)
	private TaskInstance taskInstance;
	
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
	
	public TaskInstance getTaskInstance() {
		return taskInstance;
	}
	
	public void setTaskInstance(TaskInstance taskInstance) {
		this.taskInstance = taskInstance;
	}
	
	public ProcessInstance getProcessInstance() {
		return processInstance;
	}
	
	public void setProcessInstance(ProcessInstance processInstance) {
		this.processInstance = processInstance;
	}
	
	public abstract void setValue(Object value);
	public abstract Object getValue();
}
