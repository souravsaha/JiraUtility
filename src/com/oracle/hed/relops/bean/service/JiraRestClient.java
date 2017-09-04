package com.oracle.hed.relops.bean.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import com.oracle.hed.relops.bean.excel.Type;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.oracle.excel.util.helper.CommonUtil;
import com.oracle.excel.util.helper.ExcelParser;
import com.oracle.hed.relops.bean.SubTaskResult;
import com.oracle.hed.relops.bean.excel.JiraSubtask;
import com.oracle.hed.relops.bean.excel.JiraTask;

import javafx.scene.control.TextArea;
//import javafx.scene.text.TextArea;

/**
 *
 * @author Tapas Ranjan Joshi, Sourav, Raju
 *
 */
public class JiraRestClient {

	/* private static final String DEFAULT_CONTENT_TYPE = "text/plain"; */
	private static final String CONNECTION_TIMEOUT_CODE = "10";
	private static final String READ_TIMEOUT_CODE = "20";
	private static final String GENERAL_EXCEPTION = "30";

	private static final String UNKNOWN_HOST_FAULT_CODE = "0"; // indicating not
																// in oracle's
																// network
	private static final String SERVER_FAULT_CODE = "1"; // indicating 5xx
															// status code
	private static final int RETRY_COUNT = 3;

	private static final int CONNECTION_TIMEOUT = 30000; // in milliseconds
	private static final int READ_TIMEOUT = 30000; // in milliseconds
	private static final int TIME_QUANTA = 10000; // in milliseconds

	public static final String SUBTASK_URL_PATTERN = "https://jira-uat.us.oracle.com/jira/browse/";

	private String user;
	private String pass;
	private TextArea log;

	private String jiraUrl;
	private String template;

	public String getJiraUrl() {
		return jiraUrl;
	}

	public void setJiraUrl(String jiraUrl) {
		this.jiraUrl = jiraUrl;
	}

	public String getActualUrl() {
		if (getJiraUrl().equals("UAT"))
			return "https://jira-uat.us.oracle.com";
		else
			return "https://jira.oraclecorp.com";
	}

	/**
	 *
	 * @param get
	 *            GetMethod
	 * @return java.lang.String
	 * @throws IOException
	 */
	private String getResponse(GetMethod get) throws IOException {

		InputStream is = get.getResponseBodyAsStream();

		final char[] buffer = new char[10240];
		final StringBuilder sb = new StringBuilder();
		Reader in = new InputStreamReader(is, "UTF-8");
		for (;;) {
			int rsz = in.read(buffer, 0, buffer.length);
			if (rsz < 0)
				break;
			sb.append(buffer, 0, rsz);
		}

		get.releaseConnection();
		return sb.toString();
	}

	/**
	 *
	 * @param get
	 * @return java.lang.String
	 * @throws IOException
	 */
	private String getResponse(PostMethod get) throws IOException {

		InputStream is = get.getResponseBodyAsStream();

		final char[] buffer = new char[10240];
		final StringBuilder sb = new StringBuilder();
		Reader in = new InputStreamReader(is, "UTF-8");
		for (;;) {
			int rsz = in.read(buffer, 0, buffer.length);
			if (rsz < 0)
				break;
			sb.append(buffer, 0, rsz);
		}

		get.releaseConnection();
		return sb.toString();
	}

	public JiraRestClient(String user, String pass, TextArea textArea) {
		this.user = user;
		this.pass = pass;
		this.log = textArea;
	}

	public JiraRestClient(String user, String pass) {
		this.user = user;
		this.pass = pass;
	}

	public JiraRestClient(String user, String pass, TextArea textArea, String jiraUrl, String template) {
		this.user = user;
		this.pass = pass;
		this.log = textArea;
		this.jiraUrl = jiraUrl;
		this.template = template;
	}

	/**
	 *
	 * @param URL
	 *            java.lang.String
	 * @param jsonString
	 *            java.lanag.String
	 * @return java.lang.String
	 */
	public String post(String URL, String jsonString, int readTimeout) {

		try {

			HttpState state = new HttpState();
			HttpClient client = new HttpClient();
			client.getHttpConnectionManager().getParams().setConnectionTimeout(CONNECTION_TIMEOUT);

			client.getParams().setCookiePolicy(CookiePolicy.RFC_2109);

			client.getParams().setParameter(HttpClientParams.USER_AGENT, "Mozilla/5.0");

			client.getParams().setParameter(HttpClientParams.ALLOW_CIRCULAR_REDIRECTS, true);
			client.setState(state);

			// Using GET for redirection since I guess keep getting 500 error
			// if i use POST.
			GetMethod getMethod = new GetMethod(URL);
			client.executeMethod(getMethod);
			String resp = getResponse(getMethod);
			
			//JsonParser.configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
			Document doc = Jsoup.parse(resp, URL);

			// Did we end up on the SSO page?
			Elements action = doc.select("input[name=p_submit_url]");
			if (action != null && action.size() > 0) {

				String p_submit_url = action.get(0).val();

				String site2pstoretoken = doc.select("input[name=site2pstoretoken]").get(0).val();

				String ssousername = this.user;
				String password = this.pass;

				PostMethod post = new PostMethod(p_submit_url);
				post.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
				post.addParameter("ssousername", ssousername);
				post.addParameter("password", password);
				post.addParameter("site2pstoretoken", site2pstoretoken);

				// this is where authentication happens
				client.executeMethod(post);

				// Were we redirected back to where we came from?
				if (post.getStatusCode() == 302) {
					String location = post.getResponseHeader("Location").getValue();
					post.releaseConnection();

					GetMethod get = new GetMethod(location);
					/*
					 * URL here is with SSO successful token - this doesnt
					 * support POST so have to do a GET which inturn would make
					 * a GET call for actual serivce This might result in an
					 * error GET not supported since the call is ment for POST
					 * but we still have to make a GET call.
					 */
					client.executeMethod(get);

					PostMethod postMethod = new PostMethod(URL);
					StringRequestEntity requestEntity = new StringRequestEntity(jsonString, "application/json",
							"UTF-8");

					postMethod.setRequestEntity(requestEntity);
					// This is actual call which would make a POST call to
					// service URL after SSO.
					client.getHttpConnectionManager().getParams().setSoTimeout(readTimeout);
					client.executeMethod(postMethod);
					resp = getResponse(postMethod);
					
					
					System.out.println("response got while invoking post method" + resp);
					if (postMethod.getStatusCode() == 200) {
						System.out.println("Call to Post method was successful");
					}
					if (postMethod.getStatusCode() >= 500) {
						System.out.println("Server side error occurred");
						log.appendText("Server side error occurred");
						return SERVER_FAULT_CODE;
					}
				}
			}
			return resp;

		} catch (UnknownHostException e) {
			System.out.println("Unknown host " + URL);
			return UNKNOWN_HOST_FAULT_CODE;
		} catch (ConnectTimeoutException e) {
			System.out.println("Client Timeout occured " + e.getMessage());
			log.appendText("Client Timeout occured " + e.getMessage());
			return CONNECTION_TIMEOUT_CODE;
		} catch (SocketTimeoutException e) {
			System.out.println("Reade Timeout occured " + e.getMessage());
			log.appendText("Reade Timeout occured " + e.getMessage());
			return READ_TIMEOUT_CODE;
		} catch (Exception ex) {
			System.out.println("ERROR: Getting " + URL + "; " + ex.getMessage());
			log.appendText("ERROR: Getting " + URL + "; " + ex.getMessage());
			return GENERAL_EXCEPTION;
		}
	}

	public ArrayList<SubTaskResult> processExcel(InputStream stream, String template) {
		ArrayList<SubTaskResult> list = null;
		try {

			if (template.equals("Sub Task")) {
				list = processExcelToJira(stream);

			} else {
				list = processNewExcelToJira(stream);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	// parse new Excel Template with issues i.e story,task
	public ArrayList<SubTaskResult> processNewExcelToJira(InputStream stream) throws InterruptedException {
		// for task list
		List<JiraTask> taskList = null;

		ExcelParser excelParser = new ExcelParser(log, 7);

		ArrayList<SubTaskResult> resultList = new ArrayList<SubTaskResult>();

		taskList = excelParser.parseExcel(stream, JiraTask.class);
		int rowCount=2;
		System.out.println("Task List Size: "+ taskList.size());
		
		if (taskList != null) {
			System.out.println("New Template Excel Output: ");
			for (JiraTask taskIterator : taskList) {
				System.out.println("task output: " + taskIterator.toString());
				log.appendText("processing row :"+rowCount+"\n");
				rowCount++;
				
				boolean isTaskExists = false;
				
				log.appendText(
						"\n\nTrying to create Task :" + "<" + taskIterator.getEpic() + "," + taskIterator.getTaskType()
								+ "," + taskIterator.getTaskSummary() + "," + taskIterator.getAssignee() + ","
								+ taskIterator.getComponents() + taskIterator.getDescription() + ">\n");

				if (taskIterator.getProjectCode().equals("") || taskIterator.getProjectCode() == null) {
					continue;
				}

				// no need to check parent task

				int retryCount = RETRY_COUNT;
				boolean retryNeeded;
				String response = null;
				int readTimeout = READ_TIMEOUT;
				// String url =
				// "https://jira-uat.us.oracle.com/jira/rest/api/2/issue";

				String url = getActualUrl() + "/jira/rest/api/2/issue";

				// generate body
			/*	String body = "{\r\n    \"fields\":\r\n    {\r\n        \"project\":\r\n        {\r\n            \"key\": \""
						+ taskIterator.getProjectCode()
						+ "\"\r\n        },\r\n        \"parent\":\r\n        {\r\n            \"key\": \""
						+ taskIterator.getEpic() + "\"\r\n        },\r\n        \"summary\": \""
						+ taskIterator.getTaskSummary() + " \",\r\n        \"description\": \""
						+ taskIterator.getDescription() + "\",\r\n\t\t\"assignee\":\r\n\t\t{\r\n\t\t\t\"name\":\""
						+ taskIterator.getAssignee()
						+ "\"\r\n\t\t},\r\n        \"issuetype\":\r\n        {\r\n            \"id\": \""
						+ Type.DEFECT_TYPE.getType() + "\"\r\n        }\r\n    }\r\n}";*/
				String parentTag = "";
				
				if (!CommonUtil.safeTrim(taskIterator.getEpic()).isEmpty()){
				
				//parentTag = "\r\n        \"parent\":\r\n        {\r\n            \"key\": \""
				//		+ taskIterator.getEpic() + "\"\r\n        },";

					parentTag = "\r\n        \"customfield_10014\":\r\n        {\r\n            \"value\": \"key:"
							+ taskIterator.getEpic() + "\"\r\n        },";							
				
					
				}
				String body="{\r\n    \"fields\":\r\n    {\r\n        \"project\":\r\n        {\r\n            \"key\": \""
						+ taskIterator.getProjectCode()
						+ "\"\r\n        },"+parentTag+"\r\n             \"summary\": \""
						+ taskIterator.getTaskSummary() + " \",\r\n        \"description\": \""
						+ taskIterator.getDescription().replaceAll("\\n", "\\\\n") + "\",\r\n\t\t\"assignee\":\r\n\t\t{\r\n\t\t\t\"name\":\""
						+ taskIterator.getAssignee()
						+ "\"\r\n\t\t},\r\n        \"issuetype\":\r\n        {\r\n            \"id\": \""
						+ taskIterator.getTaskTypeCode().getType() + "\"\r\n        }\r\n    }\r\n}";
				System.out.println("body="+body);
				
				do {
					System.out.println("Timeout set to " + readTimeout + " milliseconds");

					response = CommonUtil.safeTrim(post(url, body, readTimeout));
					
					System.out.println("response="+response);
					retryNeeded = false;
					if (response.equals(UNKNOWN_HOST_FAULT_CODE)) {
						System.out
								.println("Couldn't locate Jira server, please ensure you're inside Oracle's intranet");
						log.appendText("Couldn't locate Jira server, please ensure you're inside Oracle's intranet\n");
					} else if (response.equals(CONNECTION_TIMEOUT_CODE)) {
						System.out.println("Please check your network connection and retry after some time");
						log.appendText("Connection timeout occured, please retry after some time\n");

					} else if (response.equals(SERVER_FAULT_CODE)) {
						SubTaskResult subTaskResult = isSubTaskCreated(taskIterator.getEpic(),
								taskIterator.getTaskSummary());
						log.appendText("Server Fault occured\n");
						if (subTaskResult == null) {
							log.appendText("Issue not created\n");
							retryNeeded = true;
							System.out.println("Retrying to post " + url + " : count=" + retryCount);
							log.appendText("Retrying to create issue: count=" + retryCount + "\n");
							retryCount--;
						} else {
							System.out.println("502 occured, but issue created");
							log.appendText("Issue created successfully with id <" + subTaskResult.getId() + ">\n");
							log.appendText("Browse the link to view the issue { " + SUBTASK_URL_PATTERN
									+ subTaskResult.getKey() + " }\n");
							resultList.add(subTaskResult);
						}

					} else if (response.equals(READ_TIMEOUT_CODE)) {
						SubTaskResult subTaskResult = isSubTaskCreated(taskIterator.getEpic(),
								taskIterator.getTaskSummary());
						log.appendText("Read Timeout occured\n");

						if (subTaskResult == null) {
							log.appendText("Issue not created\n");
							retryNeeded = true;
							readTimeout += TIME_QUANTA;
							log.appendText("Timeout increased by " + TIME_QUANTA + " milliseconds\n");
							System.out.println("Timeout increased by " + TIME_QUANTA + " milliseconds");
							System.out.println("Retrying to post " + url + " : count=" + retryCount);
							log.appendText("Retrying to create the issue : count=" + retryCount + "\n");
							retryCount--;
						} else {
							System.out.println("timeout occured, but issue got created");
							log.appendText("Issue created successfully with id <" + subTaskResult.getId() + ">\n");
							log.appendText("Browse the link to view the issue { " + SUBTASK_URL_PATTERN
									+ subTaskResult.getKey() + " }\n");
							resultList.add(subTaskResult);
						}
					} else {
						SubTaskResult subTaskResult = parseJsonString(response);
						subTaskResult.setSummary(taskIterator.getTaskSummary());
						if(subTaskResult.getId()!=null)
							log.appendText("Issue created successfully with id <" + subTaskResult.getId() + ">\n");
						else
							log.appendText("Issue is not created successfully \n");
						resultList.add(subTaskResult);
					}
				} while (retryNeeded && retryCount >= 0);
			}

		}
		return resultList;

	}

	/**
	 *
	 * @param stream
	 *            java.io.FileInputStream
	 * @param userName
	 *            java.lang.String
	 * @param password
	 *            java.lang.String
	 * @return java.util.ArrayList<SubTaskResult>
	 * @throws InterruptedException
	 */

	public ArrayList<SubTaskResult> processExcelToJira(InputStream stream) throws InterruptedException {
		// JiraRestClient fabs = new JiraRestClient(userName, password);

		List<JiraSubtask> list = null;

		ArrayList<SubTaskResult> resultList = new ArrayList<SubTaskResult>();

		// parse excel
		// ExcelParser excelParser=new ExcelParser(log);

		ExcelParser excelParser = new ExcelParser(log, 6);

		list = excelParser.parseExcel(stream, JiraSubtask.class);
		int rowCount=2;
		
		if (list != null) {
			for (JiraSubtask subtask : list) {
				boolean isTaskExists = false;

				System.out.println("");
				log.appendText("\n\nTrying to create subtask :" + "<" + subtask.getStory() + ","
						+ subtask.getSubTaskSummary() + "," + subtask.getDescription() + "," + subtask.getAssignee()
						+ "," + subtask.getOriginalEstimate() + ">\n");
				
				log.appendText("processing row :"+rowCount+"\n");
				rowCount++;
				
				if (subtask.getProjectCode().equals("") || subtask.getProjectCode() == null) {
					continue;
				}
				isTaskExists = isTaskCreated(subtask.getStory());
				if (!isTaskExists) {
					System.out.println("Issue/parent task <" + subtask.getStory()
							+ ">  doesnot exists| Skipping sub-task creation");
					log.appendText("Issue/parent task <" + subtask.getStory()
							+ ">  doesnot exists| Skipping sub-task creation\n");
					continue;
				}
				int retryCount = RETRY_COUNT;
				boolean retryNeeded;
				String response = null;
				int readTimeout = READ_TIMEOUT;
				// String url =
				// "https://jira-uat.us.oracle.com/jira/rest/api/2/issue";
				String url = getActualUrl() + "/jira/rest/api/2/issue";
				
				if(subtask.getDescription().contains("\n"))
					System.out.println("new line found"+subtask.getDescription().replaceAll("\n", " "));
				
				String body = "{\r\n    \"fields\":\r\n    {\r\n        \"project\":\r\n        {\r\n            \"key\": \""
						+ subtask.getProjectCode()
						+ "\"\r\n        },\r\n        \"parent\":\r\n        {\r\n            \"key\": \""
						+ subtask.getStory() + "\"\r\n        },\r\n        \"summary\": \""
						+ subtask.getSubTaskSummary() + " \",\r\n        \"description\": \"" + subtask.getDescription().replaceAll("\\n", "\\\\n")
						+ "\",\r\n\t\t\"assignee\":\r\n\t\t{\r\n\t\t\t\"name\":\"" + subtask.getAssignee()
						+ "\"\r\n\t\t},\r\n\t\t \"timetracking\": {\r\n            \"originalEstimate\": \""
						+ subtask.getOriginalEstimate()
						+ "\"\r\n        },\r\n        \"issuetype\":\r\n        {\r\n            \"id\": \"5\"\r\n        }\r\n    }\r\n}";

				do {
					System.out.println("Timeout set to " + readTimeout + " milliseconds");
					System.out.println("Body: \n"+body);
					response = CommonUtil.safeTrim(post(url, body, readTimeout));
					retryNeeded = false;
					if (response.equals(UNKNOWN_HOST_FAULT_CODE)) {
						System.out
								.println("Couldn't locate Jira server, please ensure you're inside Oracle's intranet");
						log.appendText("Couldn't locate Jira server, please ensure you're inside Oracle's intranet\n");
					} else if (response.equals(CONNECTION_TIMEOUT_CODE)) {
						System.out.println("Please check your network connection and retry after some time");
						log.appendText("Connection timeout occured, please retry after some time\n");

					} else if (response.equals(SERVER_FAULT_CODE)) {
						SubTaskResult subTaskResult = isSubTaskCreated(subtask.getStory(), subtask.getSubTaskSummary());
						log.appendText("Server Fault occured\n");
						if (subTaskResult == null) {
							log.appendText("Subtask not created\n");
							retryNeeded = true;
							System.out.println("Retrying to post " + url + " : count=" + retryCount);
							log.appendText("Retrying to create subtask: count=" + retryCount + "\n");
							retryCount--;
						} else {
							System.out.println("502 occured, but subtask created");
							log.appendText("Subtask created successfully with id <" + subTaskResult.getId() + ">\n");
							log.appendText("Browse the link to view the subtask { " + SUBTASK_URL_PATTERN
									+ subTaskResult.getKey() + " }\n");
							resultList.add(subTaskResult);
						}

					} else if (response.equals(READ_TIMEOUT_CODE)) {
						SubTaskResult subTaskResult = isSubTaskCreated(subtask.getStory(), subtask.getSubTaskSummary());
						log.appendText("Read Timeout occured\n");
						if (subTaskResult == null) {
							log.appendText("Subtask not created\n");
							retryNeeded = true;
							readTimeout += TIME_QUANTA;
							log.appendText("Timeout increased by " + TIME_QUANTA + " milliseconds\n");
							System.out.println("Timeout increased by " + TIME_QUANTA + " milliseconds");
							System.out.println("Retrying to post " + url + " : count=" + retryCount);
							log.appendText("Retrying to create subtask : count=" + retryCount + "\n");
							retryCount--;
						} else {
							System.out.println("timeout occured, but subtask created");
							log.appendText("Subtask created successfully with id <" + subTaskResult.getId() + ">\n");
							log.appendText("Browse the link to view the subtask { " + SUBTASK_URL_PATTERN
									+ subTaskResult.getKey() + " }\n");
							resultList.add(subTaskResult);
						}
					} else {
						SubTaskResult subTaskResult = parseJsonString(response);
						subTaskResult.setSummary(subtask.getSubTaskSummary());
						//debug 						
						if(subTaskResult.getId()!=null)
							log.appendText("Subtask created successfully with id <" + subTaskResult.getId() + ">\n");
						else
							log.appendText("Subtask is not created successfully "+ "\n");
						resultList.add(subTaskResult);
					}
				} while (retryNeeded && retryCount >= 0);
			}
		}
		else
			log.appendText("Excel sheet is not properly formatted. \n");
		return resultList;

	}

	/**
	 *
	 * @param response
	 *            java.lang.String
	 * @return SubTaskResult
	 */
	public SubTaskResult parseJsonString(String response) {
		System.out.println(response);
		JSONParser parser = new JSONParser();
		SubTaskResult task = new SubTaskResult();
		try {
			Object obj = parser.parse(response);
			JSONObject json = (JSONObject) obj;
			Set<String> set = json.keySet();
			// LabelDetails labelDetail = new LabelDetails();
			for (String s : set) {
				if (s.equals("id")) {
					task.setId((String) json.get(s));
				} else if (s.equals("key")) {
					task.setKey((String) json.get(s));
				} else if (s.equals("self")) {
					task.setSelf((String) json.get(s));
				} else if (s.equals("fields")) {
					JSONObject jsonObject = (JSONObject) json.get(s);
					task.setSummary(CommonUtil.safeTrim(getValueFromJSON(jsonObject.toJSONString(), "summary")));
				}
				System.out.println(s + " : " + json.get(s));
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return task;
	}

	/**
	 * method to call get request
	 *
	 * @param URL
	 * @return
	 */
	public String get(String URL, int readTimeout) {

		try {

			HttpState state = new HttpState();
			HttpClient client = new HttpClient();
			client.getHttpConnectionManager().getParams().setConnectionTimeout(CONNECTION_TIMEOUT);

			client.getParams().setCookiePolicy(CookiePolicy.RFC_2109);

			client.getParams().setParameter(HttpClientParams.USER_AGENT, "Mozilla/5.0");

			client.getParams().setParameter(HttpClientParams.ALLOW_CIRCULAR_REDIRECTS, true);
			client.setState(state);

			// Using GET for redirection since I guess keep getting 500 error
			// if i use POST.
			GetMethod getMethod = new GetMethod(URL);
			client.executeMethod(getMethod);
			String resp = getResponse(getMethod);
			// System.out.println(resp);

			Document doc = Jsoup.parse(resp, URL);

			// Did we end up on the SSO page?
			Elements action = doc.select("input[name=p_submit_url]");
			if (action != null && action.size() > 0) {

				String p_submit_url = action.get(0).val();

				String site2pstoretoken = doc.select("input[name=site2pstoretoken]").get(0).val();

				String ssousername = this.user;
				String password = this.pass;

				PostMethod post = new PostMethod(p_submit_url);
				post.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
				post.addParameter("ssousername", ssousername);
				post.addParameter("password", password);
				post.addParameter("site2pstoretoken", site2pstoretoken);

				// this is where authentication happens
				client.executeMethod(post);

				// Were we redirected back to where we came from?
				if (post.getStatusCode() == 302) {
					String location = post.getResponseHeader("Location").getValue();
					post.releaseConnection();

					GetMethod get = new GetMethod(location);
					/*
					 * URL here is with SSO successful token - this doesnt
					 * support POST so have to do a GET which inturn would make
					 * a GET call for actual serivce This might result in an
					 * error GET not supported since the call is ment for POST
					 * but we still have to make a GET call.
					 */
					client.executeMethod(get);

					GetMethod getIssueMethod = new GetMethod(URL);
					client.getHttpConnectionManager().getParams().setSoTimeout(readTimeout);
					client.executeMethod(getIssueMethod);
					resp = getResponse(getIssueMethod);
					System.out.println("response:" + resp);
					System.out.println("response code:" + getMethod.getStatusCode());
					if (getMethod.getStatusCode() == 200) {
						System.out.println("Call to get method was successful");
					}
					if (getMethod.getStatusCode() >= 500) {
						System.out.println("Server side error occurred");
						return SERVER_FAULT_CODE;
					}
				}
			}
			return resp;

		} catch (UnknownHostException e) {
			System.out.println("Unknown host " + URL);
			return UNKNOWN_HOST_FAULT_CODE;
		} catch (ConnectTimeoutException e) {
			System.out.println("Client Timeout occured| " + e.getMessage());
			return CONNECTION_TIMEOUT_CODE;
		} catch (SocketTimeoutException e) {
			System.out.println("Read Timeout occured| " + e.getMessage());
			return READ_TIMEOUT_CODE;
		} catch (Exception ex) {
			System.out.println("ERROR: Getting " + URL + "; " + ex.getMessage());
			return GENERAL_EXCEPTION;
		}
	}

	/**
	 * method to test whether task exists or not
	 *
	 * @param issueId
	 * @return
	 */
	public boolean isTaskCreated(String issueId) {
		boolean isTaskCreated = false;
		String response = null;
		boolean retryNeeded;
		int retryCount = RETRY_COUNT;
		issueId = CommonUtil.safeTrim(issueId);
		String url = null;
		int readTimeout = READ_TIMEOUT;
		if (!issueId.isEmpty()) {
			do {
				System.out.println("Timeout set to " + readTimeout + " milliseconds");
				// url = "https://jira-uat.us.oracle.com/jira/rest/api/2/issue/"
				// + issueId;
				url = getActualUrl() + "/jira/rest/api/2/issue/" + issueId;

				response = CommonUtil.safeTrim(get(url, readTimeout));
				retryNeeded = false;
				if (response.equals(UNKNOWN_HOST_FAULT_CODE)) {
					System.out.println("Couldn't locate Jira server, please ensure you're inside Oracle's intranet ");
					log.appendText("Couldn't locate Jira server, please ensure you're inside Oracle's intranet\n");
				} else if (response.contains("Issue Does Not Exist")) {
					isTaskCreated = false;
					log.appendText("Parent task/issue id <" + issueId + "> not found\n");
				} else if (response.equals(SERVER_FAULT_CODE)) {
					retryNeeded = true;
					log.appendText("Server Fault occured\n");
					System.out.println("Retrying to get " + url + " : count=" + retryCount);
					log.appendText(
							"Retrying to get parent task/issue id <" + issueId + "> : count=" + retryCount + "\n");
					retryCount--;
				} else if (response.equals(READ_TIMEOUT_CODE)) {
					retryNeeded = true;
					log.appendText("Read Timeout occured\n");
					readTimeout += TIME_QUANTA;
					System.out.println("Timeout increased by " + TIME_QUANTA + " milliseconds");
					log.appendText("Timeout increased by " + TIME_QUANTA + " milliseconds\n");
					System.out.println("Retrying to get " + url + " : count=" + retryCount);
					log.appendText(
							"Retrying to get parent task/issue id <" + issueId + "> : count=" + retryCount + "\n");
					retryCount--;

				} else if (response.equals(CONNECTION_TIMEOUT_CODE)) {
					System.out.println("Please check your network connection and retry after some time");
					log.appendText("Connection timeout occured, please retry after some time\n");
				} else {
					String id = CommonUtil.safeTrim(getValueFromJSON(response, "id"));
					if (!id.isEmpty()) {
						isTaskCreated = true;
						log.appendText("Parent task/issue id <" + issueId + "> found\n");
					} else {
						log.appendText("Parent task/issue id <" + issueId + "> not found\n");
					}
				}
			} while (retryNeeded && (retryCount >= 0));
		}
		return isTaskCreated;
	}

	/**
	 *
	 * @param jsonTextArray
	 * @return
	 */
	private List<SubTaskResult> parseJSONArrayOfSubstasks(String jsonTextArray) {
		List<SubTaskResult> subTaskResults = new ArrayList<SubTaskResult>();
		JSONParser parser = new JSONParser();
		JSONArray jsonArray = null;
		try {
			jsonArray = (JSONArray) parser.parse(jsonTextArray);
			for (int i = 0; i < jsonArray.size(); i++) {
				SubTaskResult subTask = parseJsonString(jsonArray.get(i).toString());
				subTaskResults.add(subTask);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return subTaskResults;
	}

	/**
	 *
	 * @param parentTaskId
	 * @return
	 */
	private List<SubTaskResult> getSubtasks(String parentTaskId) {

		String response = null;
		boolean retryNeeded = false;
		int retryCount = RETRY_COUNT;
		parentTaskId = CommonUtil.safeTrim(parentTaskId);
		String url = null;
		List<SubTaskResult> subTaskResults = null;
		int readTimeout = READ_TIMEOUT;
		if (!parentTaskId.isEmpty()) {
			do {
				System.out.println("Timeout set to " + readTimeout + " milliseconds");
				//url = "https://jira-uat.us.oracle.com/jira/rest/api/2/issue/" + parentTaskId;
				url = getActualUrl()+"/jira/rest/api/2/issue/" + parentTaskId;
				response = CommonUtil.safeTrim(get(url, readTimeout));
				retryNeeded = false;
				if (response.equals(UNKNOWN_HOST_FAULT_CODE)) {
					System.out.println("Couldn't locate Jira server, please ensure you're inside Oracle's intranet ");
					log.appendText("Couldn't locate Jira server, please ensure you're inside Oracle's intranet\n");
				} else if (response.contains("Issue Does Not Exist")) {
					retryNeeded = false;
					log.appendText("Parent task/issue id <" + parentTaskId + "> not found\n");
				} else if (response.equals(SERVER_FAULT_CODE)) {
					retryNeeded = true;
					log.appendText("Server Fault occured\n");
					readTimeout += TIME_QUANTA;
					System.out.println("Timeout increased by " + TIME_QUANTA + " milliseconds");
					System.out.println("Retrying to get " + url + " : count=" + retryCount);
					log.appendText(
							"Retrying to get parent task/issue id <" + parentTaskId + "> : count=" + retryCount + "\n");
					retryCount--;
				} else if (response.equals(READ_TIMEOUT_CODE)) {
					retryNeeded = true;
					log.appendText("Read Timeout occured\n");
					readTimeout += TIME_QUANTA;
					System.out.println("Timeout increased by " + TIME_QUANTA + " milliseconds");
					log.appendText("Timeout increased by " + TIME_QUANTA + " milliseconds\n");
					System.out.println("Retrying to get " + url + " : count=" + retryCount);
					log.appendText(
							"Retrying to get parent task/issue id <" + parentTaskId + "> : count=" + retryCount + "\n");
					retryCount--;

				} else if (response.equals(CONNECTION_TIMEOUT_CODE)) {
					System.out.println("Please check your network connection and retry after some time");
					log.appendText("Connection timeout occured, please retry after some time\n");
				} else {
					String id = CommonUtil.safeTrim(getValueFromJSON(response, "id"));
					retryNeeded = false;
					if (!id.isEmpty()) {
						log.appendText("Parent task/issue id <" + parentTaskId + "> found\n");
						String fields = CommonUtil.safeTrim(getValueFromJSON(response, "fields"));
						String subtasks = CommonUtil.safeTrim(getValueFromJSON(fields, "subtasks"));
						subTaskResults = parseJSONArrayOfSubstasks(subtasks);
					} else {
						log.appendText("Parent task/issue id <" + parentTaskId + "> not found\n");
					}
				}
			} while (retryNeeded && (retryCount >= 0));
		}
		return subTaskResults;
	}

	/**
	 * Checks if subtask is created with a given summary under parent task/issue
	 * id
	 *
	 * @param parentTaskId
	 * @param subTaskSummary
	 * @return
	 */
	public SubTaskResult isSubTaskCreated(String parentTaskId, String subTaskSummary) {
		List<SubTaskResult> subTaskResults = getSubtasks(parentTaskId);
		boolean isSubtaskCreated = false;
		int count = 0;
		SubTaskResult substask = null;
		if (subTaskResults != null) {
			for (SubTaskResult subTaskResult : subTaskResults) {
				System.out.println(subTaskResult);
				if (CommonUtil.safeTrim(subTaskResult.getSummary()).toUpperCase()
						.equals(CommonUtil.safeTrim(subTaskSummary).toUpperCase())) {
					isSubtaskCreated = true;
					substask = subTaskResult;
					log.appendText("Subtask with summary='" + subTaskSummary + "' with parent-task-id:<" + parentTaskId
							+ "> found, subtask-id: <" + substask.getId() + ">\n");
					break;
				} else {
					count++;
				}
			}
			if (count == subTaskResults.size()) {
				log.appendText("Subtask with summary='" + subTaskSummary + "' with parent-task-id:<" + parentTaskId
						+ "> not found \n");
			}
		}
		return substask;
	}

	/**
	 * Gets value from JSON object
	 *
	 * @param jsonText
	 * @param key
	 * @return
	 */
	public String getValueFromJSON(String jsonText, String key) {

		JSONParser parser = new JSONParser();
		String value = "";
		try {
			Object obj = parser.parse(jsonText);
			JSONObject json = (JSONObject) obj;
			value = String.valueOf(json.get(key));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return value;
	}

}
