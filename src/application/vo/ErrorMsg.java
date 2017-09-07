/**
 * 
 */
package application.vo;

import java.util.List;

/**
 * @author soursaha
 *
 */
public class ErrorMsg {
	
	private ErrorString errors;
	private List<String> errorMessages;
	
	public List<String> getErrorMessages() {
		return errorMessages;
	}

	public void setErrorMessages(List<String> errorMessages) {
		this.errorMessages = errorMessages;
	}

	public ErrorString getErrors() {
		return errors;
	}

	public void setErrors(ErrorString errors) {
		this.errors = errors;
	}

	@Override
	public String toString() {
		return "ErrorMsg [errors=" + errors + "]";
	}
		
}
