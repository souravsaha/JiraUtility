/**
 * 
 */
package com.oracle.excel.test;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import com.oracle.excel.util.helper.ExcelParser;
import com.oracle.hed.relops.bean.excel.JiraTask;
import com.oracle.hed.relops.bean.service.JiraRestClient;

import javafx.scene.control.TextArea;

/**
 * @author soursaha
 *
 */
public class Test {

	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub
		

				
		InputStream inputStream = null;
		try 
		{
			inputStream = new FileInputStream("D:/StudentCloud/JiraTool/JiraTaskDefectTemplate.xlsx");
			ExcelParser e=new ExcelParser(7);
			List<JiraTask> taskList=e.parseExcel(inputStream, JiraTask.class);
			
			if(taskList != null)
			{
				System.out.println("New Template Excel Output: ");
				for (JiraTask subtaskIterator : taskList)
				{
					System.out.println("task output: " + subtaskIterator.toString());
																
				}
			}
		} 
		catch (FileNotFoundException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		new BufferedInputStream(inputStream);

		
		
	}

}
