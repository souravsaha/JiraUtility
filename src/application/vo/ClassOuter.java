package application.vo;

public class ClassOuter {

	private ClassInner fields;

	public ClassInner getFields() {
		return fields;
	}

	public void setFields(ClassInner fields) {
		this.fields = fields;
	}

	@Override
	public String toString() {
		return "ClassOuter [fields=" + fields + "]";
	}

}
