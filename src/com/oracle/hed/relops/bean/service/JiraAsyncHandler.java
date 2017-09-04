/**
 *
 */
package com.oracle.hed.relops.bean.service;

import java.io.InputStream;
import java.util.List;

import com.oracle.excel.util.helper.CommonUtil;
import com.oracle.hed.relops.bean.SubTaskResult;

import application.UiController;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert.AlertType;





/**
 * @author raparash
 *
 */
public class JiraAsyncHandler implements Runnable{

	private String username;
	private String password;
	private InputStream inputStream;
	private TextArea textArea;
	
	private String jiraUrl;
	private String template;

	private Alert alert;

	public JiraAsyncHandler(String username,String password,InputStream inputStream,TextArea textArea, String jiraUrl, String template, Alert alert) {
		this.username=username;
		this.password=password;
		this.inputStream=inputStream;
		this.textArea=textArea;
		this.jiraUrl=jiraUrl;
		this.template=template;
		this.alert = alert;
	}

	public void processTask(){
		JiraRestClient restClient=new JiraRestClient(username, password,textArea,jiraUrl,template);
		List<SubTaskResult> listOfCreatedTasks=null;
		
		String taskType;
			textArea.appendText("\nProcessing Started\n");
			textArea.appendText( "This might take few minutes\n");
			
			// once template value of task/story/defect changes to Issue it does not require anymore
			if(template.equals("Sub Task"))
				taskType="SubTask";
			else
				taskType="Issue";
			
			try {
				listOfCreatedTasks=restClient.processExcel(inputStream,template);
				
				// try to extract new template 
				
				textArea.appendText("\n\n---------------------------------------------Summary-------------------------------------------\n\n");
				// append to a html file and show the link
				
				if(listOfCreatedTasks==null || listOfCreatedTasks.size()==0)
				{
					textArea.appendText("No Tasks/Issues created");
				}
				else
				{
					for(SubTaskResult subtask:listOfCreatedTasks)
					{
						String key=CommonUtil.safeTrim(subtask.getKey());
		    			if(!key.isEmpty())
		    			{
		    				//textArea.appendText("Subtask created successfully: "+key+" - "+subtask.getSummary()+" ( "+JiraRestClient.SUBTASK_URL_PATTERN+key+" )\n");
		    				textArea.appendText(taskType+" created successfully: "+key+" - "+subtask.getSummary()+" ( "+restClient.getActualUrl()+"/jira/browse/"+key+" )\n");
		    			}
					}
				}
				textArea.appendText("\n--------------------------------------------------------------------------------------------\n");
				// labda function to move the conrtrol to ui
				Platform.runLater(    
				    ()-> {
				        // Update UI here.
				    	alert.show();
				    }
				);
				
								
			} catch (Exception e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	
	
	@Override
	public void run() {
		processTask();
		
		//UiController.generateAlertLog();
	}

}
