/**
 *
 */
package application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.net.URL;
import java.util.ResourceBundle;

import com.oracle.excel.util.helper.CommonUtil;
import com.oracle.hed.relops.bean.service.JiraAsyncHandler;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
//import javafx.scene.text.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 * @author raparash,sourav
 *
 */
public class UiController implements Initializable {

	@FXML
	private TextField username;

	@FXML
	private TextField password;

	@FXML
	private TextField browse;

	@FXML
	private Button submit;

	@FXML
	private Button download;

	@FXML
	private Button browseButton;

	@FXML
	private Button cancel;

	@FXML
	private TextArea log;

	@FXML
	private ChoiceBox<String> jira;

	@FXML
	private ChoiceBox<String> templateCBox;
	
	@FXML
	private Button clear;
		
	private Alert alert;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub

		getJira();
		getTemplateCBox();
		log.setEditable(false);
		generateAlertLog();
		
	}
	/**
	 * @return the username
	 */
	public TextField getUsername() {
		return username;
	}

	/**
	 * @param username the username to set
	 */
	public void setUsername(TextField username) {
		this.username = username;
	}

	/**
	 * @return the password
	 */
	public TextField getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(TextField password) {
		this.password = password;
	}

	/**
	 * @return the browse
	 */
	public TextField getBrowse() {
		return browse;
	}

	/**
	 * @param browse the browse to set
	 */
	public void setBrowse(TextField browse) {
		this.browse = browse;
	}

	/**
	 * @return the browseButton
	 */
	public Button getBrowseButton() {
		return browseButton;
	}

	/**
	 * @param browseButton the browseButton to set
	 */
	public void setBrowseButton(Button browseButton) {
		this.browseButton = browseButton;
	}

	/**
	 * @return the submit
	 */
	public Button getSubmit() {
		return submit;
	}

	/**
	 * @param submit the submit to set
	 */
	public void setSubmit(Button submit) {
		this.submit = submit;
	}

	/**
	 * @return the download
	 */
	public Button getDownload() {
		return download;
	}

	/**
	 * @param download the download to set
	 */
	public void setDownload(Button download) {
		this.download = download;
	}

	/**
	 * @return the cancel
	 */
	public Button getCancel() {
		return cancel;
	}

	/**
	 * @param cancel the cancel to set
	 */
	public void setCancel(Button cancel) {
		this.cancel = cancel;
	}

	/**
	 * @return the log
	 */
	public TextArea getLog() {
		return log;
	}

	/**
	 * @param log the log to set
	 */
	public void setLog(TextArea log) {
		this.log = log;
	}

	/**
	 * @return the jira
	 */
	public ChoiceBox<String> getJira() {
		
		
		// added list of values 
		ObservableList<String> list = FXCollections.observableArrayList("UAT","Production");
		jira.setItems(list);
		
		return jira;
	}

	/**
	 * @return the jira
	 */
	public ChoiceBox<String> getTemplateCBox() {
		
		
		// added list of values 
		ObservableList<String> list = FXCollections.observableArrayList("Issue","Sub Task");
		templateCBox.setItems(list);
		
		return templateCBox;
	}
	
	/**
	 * @param jira the jira to set
	 */
	public void setJira(ChoiceBox<String> jira) {
		this.jira = jira;
	}


	/**
	 * @return the clear
	 */
	public Button getClear() {
		return clear;
	}

	/**
	 * @param clear the clear to set
	 */
	public void setClear(Button clear) {
		this.clear = clear;
	}

	public void generateAlertLog()
	{
		alert = new Alert(AlertType.INFORMATION);
		alert.setHeaderText("Task Completed");
		String s ="Your task got completed  ";
		alert.setContentText(s);
		//alert.show();
	}
	
	public boolean checkValidFile()
	{
		File file = new File(browse.getText());
		return (file.exists()) ? true : false; 
	}
	
	public boolean checkAllFields()
	{
		if(username.getText().trim().isEmpty() || password.getText().trim().isEmpty() || browse.getText().trim().isEmpty() || jira.getSelectionModel().isEmpty() || templateCBox.getSelectionModel().isEmpty())
			return true;
		else 
			return false;
						
	}
	
	public void submitOnClick(ActionEvent event) throws InterruptedException{
		
		//check if all fields are there
		if(checkAllFields())
		{
			log.appendText("All fields are mandatory. Please give all the inputs\n");
			return;
		}
		
		// check if the file path is correct
		if(!checkValidFile())
		{
			log.appendText("File path is not correct. Please give correct file \n ");
			return;
		}
			
		
		
		Thread jiraTaskhandler=null;
		try {
			// pass template in the constructor
			jiraTaskhandler=new Thread(new JiraAsyncHandler(username.getText(), password.getText(), new FileInputStream(browse.getText()), log, jira.getSelectionModel().getSelectedItem().toString(),templateCBox.getSelectionModel().getSelectedItem().toString(),alert));
			
			jiraTaskhandler.start();
			
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		if(jiraTaskhandler.isAlive())
		{
			generateAlertLog();
		}

	}

	public void showFileDialog(ActionEvent event){
		FileChooser fileChooser=new FileChooser();
		fileChooser.setTitle("Browse File");
		 fileChooser.getExtensionFilters().addAll(
		         new ExtensionFilter("Excel 2007 Files", "*.xlsx"),
		         new ExtensionFilter("Excel 2003 Files", "*.xls"),
		         new ExtensionFilter("All Files", "*.*"));
		 File file=fileChooser.showOpenDialog(browseButton.getScene().getWindow());
		if(file!=null)
		 getBrowse().setText(file.getAbsolutePath());
	}

	public void clearLogs(ActionEvent event){
		log.clear();
	}

	public void closeApplication(ActionEvent event){
		System.exit(0);
	}

	public void showSaveFileDialog(ActionEvent event){
		FileChooser fileChooser=new FileChooser();
		fileChooser.setTitle("Save File");
		 fileChooser.getExtensionFilters().addAll(
		         new ExtensionFilter("Text Files", "*.txt"),
		         new ExtensionFilter("All Files", "*.*"));

		 File file=fileChooser.showSaveDialog(download.getScene().getWindow());
		 if(file!=null){
			 saveFile(file, log.getText());
		 }
	}

	private void saveFile(File file,String content){
		FileWriter fileWriter=null;
		try{
			fileWriter=new FileWriter(file);
			fileWriter.write(content);
			fileWriter.flush();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			CommonUtil.safeClose(fileWriter);
		}
	}

}
