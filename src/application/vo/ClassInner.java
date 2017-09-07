package application.vo;

import java.util.ArrayList;
import java.util.List;

public class ClassInner {

	private Value project;
	private Value parent;
	private String summary;
	private String description;
	private Value assignee;
	private Value timetracking;
	private Value issuetype;
	private List<Value> components;
	private String customfield_10014;

	public void setName(String name) {
		if (assignee == null)
			assignee = new Value();

		assignee.setName(name);
	}

	public void setProjectKey(String key) {
		if (project == null)
			project = new Value();

		project.setKey(key);
	}

	public void setParentKey(String key) {
		if (parent == null)
			parent = new Value();

		parent.setKey(key);
	}

	public void setOriginalEstimate(String originalEstimate) {
		if (timetracking == null)
			timetracking = new Value();

		timetracking.setOriginalEstimate(originalEstimate);
	}

	public void setIssueTypeId(String issuetype) {
		if (this.issuetype == null)
			this.issuetype = new Value();

		this.issuetype.setId(issuetype);
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Value getProject() {
		return project;
	}

	public Value getParent() {
		return parent;
	}

	public Value getAssignee() {
		return assignee;
	}

	public Value getTimetracking() {
		return timetracking;
	}

	public Value getIssuetype() {
		return issuetype;
	}

	public void addComponent(String componentId) {
		if (componentId == null)
			return;

		if (components == null)
			components = new ArrayList<>();

		Value v = new Value();
		v.setId(componentId);
		components.add(v);
	}

	public String getCustomfield_10014() {
		return customfield_10014;
	}

	public void setCustomfield_10014(String customfield_10015) {
//		if (this.customfield_10015 == null)
//			this.customfield_10015 = new Value();
//
//		this.customfield_10015.setValue(customfield_10015);
		this.customfield_10014=customfield_10015;
	}

	@Override
	public String toString() {
		return "ClassInner [project=" + project + ", parent=" + parent + ", summary=" + summary + ", description="
				+ description + ", assignee=" + assignee + ", timetracking=" + timetracking + ", issuetype=" + issuetype
				+ "]";
	}

}
