/**
 * 
 */
package com.oracle.hed.relops.bean.excel;

/**
 * @author Sourav
 *
 */
public class JiraTask implements ExcelBean{

	private String projectCode;
	private String epic;
	private String taskType;
	private String taskSummary;
	private String assignee;
	private String components;
	private String description;
	private Type taskTypeCode;
	

	/**
	 * @return the projectCode
	 */
	public String getProjectCode() {
		return projectCode;
	}
	/**
	 * @param projectCode the projectCode to set
	 */
	public void setProjectCode(String projectCode) {
		this.projectCode = projectCode;
	}
	/**
	 * @return the story
	 */
	public String getEpic() {
		return epic;
	}
	/**
	 * @param story the story to set
	 */
	public void setEpic(String epic) {
		this.epic = epic;
	}
	/**
	 * @return the subTaskType
	 */
	public String getTaskType() {
		return taskType;
	}
	/**
	 * @param subTaskType the subTaskType to set
	 */
	public void setTaskType(String taskType) {
		this.taskType = taskType;
		if(taskType.equalsIgnoreCase("Defect"))
			setTaskTypeCode(Type.DEFECT_TYPE);
		else if(taskType.equalsIgnoreCase("Story"))
			setTaskTypeCode(Type.STORY_TYPE);
		else
			setTaskTypeCode(Type.TASK_TYPE);
			
	}
	
	/**
	 * @return the subTaskSummary
	 */
	public String getTaskSummary() {
		return taskSummary;
	}
	/**
	 * @param subTaskSummary the subTaskSummary to set
	 */
	public void setTaskSummary(String taskSummary) {
		this.taskSummary = taskSummary;
	}
	
	/**
	 * @return the assignee
	 */
	public String getAssignee() {
		return assignee;
	}
	/**
	 * @param assignee the assignee to set
	 */
	public void setAssignee(String assignee) {
		this.assignee = assignee;
	}
	/**
	 * @return the components
	 */
	public String getComponents() {
		return components;
	}
	/**
	 * @param components the components to set
	 */
	public void setComponents(String components) {
		this.components = components;
	}
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.oracle.excel.util.bean.ExcelBean#convertToBean(com.oracle.excel.util.bean.ExcelRow)
	 */
	@Override
	public JiraTask convertToBean(ExcelRow excelRow) {
		
		this.setProjectCode(excelRow.getColumnValue(0));
		this.setEpic(excelRow.getColumnValue(1));
		this.setTaskType(excelRow.getColumnValue(2));
		this.setTaskSummary(excelRow.getColumnValue(3));
		this.setAssignee(excelRow.getColumnValue(4));
		this.setComponents(excelRow.getColumnValue(5));
		this.setDescription(excelRow.getColumnValue(6));
		
		return this;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Jiratask [projectCode=" + projectCode + ", epic=" + epic + ", taskType=" + taskType + ", taskSummary=" + taskSummary
				+ ", assignee=" + assignee + ", components=" + components + ", description=" + description
				+ "]";
	}
	/**
	 * @return the taskTypeCode
	 */
	public Type getTaskTypeCode() {
		return taskTypeCode;
	}
	/**
	 * @param taskTypeCode the taskTypeCode to set
	 */
	private void setTaskTypeCode(Type taskTypeCode) {
		this.taskTypeCode = taskTypeCode;
	}
	
}
