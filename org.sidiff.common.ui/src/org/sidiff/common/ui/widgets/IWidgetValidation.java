package org.sidiff.common.ui.widgets;




public interface IWidgetValidation {

	/**
	 * Validate the actual state of the widget.
	 *
	 * @return <code>true</code> if everything is fine; <code>false</code> otherwise.
	 */
	public boolean validate();

	/**
	 * 
	 * @return An warning or error message about the actual state of the widget.
	 */
	public ValidationMessage getValidationMessage();
		
	

	public class ValidationMessage{
		private String message;
		private ValidationType type;
	
		public ValidationMessage(ValidationType type, String message){
			this.message = message;
			this.type = type;
		}
	
		public ValidationType getType(){
			return type;
		}
		
		public String getMessage(){
			return message;
		}
	
		public enum ValidationType{
			OK, ERROR, WARNING
		}
	}
}
