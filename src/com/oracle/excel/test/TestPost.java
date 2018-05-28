/**
 * 
 */
package com.oracle.excel.test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import com.oracle.excel.util.helper.ExcelParser;
import com.oracle.hed.relops.bean.excel.JiraTask;
import com.oracle.hed.relops.bean.service.JiraRestClient;

/**
 * @author soursaha
 *
 */
public class TestPost {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		final String pass = "******";
		final String uatUrl = "https://jira-uat.us.oracle.com/jira/rest/api/2/issue";
		
		JiraRestClient jiraClient=new JiraRestClient("sourav.saha@oracle.com", pass);
		
		
		InputStream inputStream = null;

		try {
			inputStream = new FileInputStream("D:/StudentCloud/JiraTool/JiraTaskDefectTemplate.xlsx");
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		ExcelParser e=new ExcelParser(7);
		List<JiraTask> taskList=e.parseExcel(inputStream, JiraTask.class);
		
		if(taskList != null)
		{
			System.out.println("New Template Excel Output: ");
			for (JiraTask taskIterator : taskList)
			{
				System.out.println("task output: " + taskIterator.toString());
		
				String body = "{\r\n    \"fields\":\r\n    {\r\n        \"project\":\r\n        {\r\n            \"key\": \""
						+ taskIterator.getProjectCode()
						+ "\"\r\n        },\r\n        \"parent\":\r\n        {\r\n            \"key\": \""
						+ taskIterator.getEpic() + "\"\r\n        },\r\n        \"summary\": \""
						+ taskIterator.getTaskSummary() + " \",\r\n        \"description\": \"" + taskIterator.getDescription()
						+ "\",\r\n\t\t\"assignee\":\r\n\t\t{\r\n\t\t\t\"name\":\"" + taskIterator.getAssignee()
						+ "\"\r\n\t\t},\r\n        \"issuetype\":\r\n        {\r\n            \"id\": \"3\"\r\n        }\r\n    }\r\n}";
				
				System.out.println(body);
				
				System.out.println(jiraClient.post(uatUrl, body, 30000));

			}
		}
		
	}

}
