/**
 * 
 */
package com.oracle.hed.relops.bean.excel;

/**
 * @author soursaha
 *
 */
public enum Type {
	
	
		STORY_TYPE("7"), TASK_TYPE("3"),DEFECT_TYPE("25");

		private String type;
		private Type(String type){
			this.type=type;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}

		


}
