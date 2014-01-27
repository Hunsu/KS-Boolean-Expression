package com.ksbooleanexpression;

public class SyntaxErrorException extends Exception {

	private String message;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SyntaxErrorException(){
		message = Tools.getLocalizedString("SyntaxError")+ " : "+
		Tools.getLocalizedString("ArithmeticError");
	}
	
	public SyntaxErrorException(String cause, int position) {
		message = Tools.getLocalizedString("SyntaxError") + " : " +
		Tools.getLocalizedString("Cause")+ " : " + cause   + " : " +  
		Tools.getLocalizedString("Position") +  " : " + position;
		
	}

	public SyntaxErrorException(String cause) {
		message = Tools.getLocalizedString("SyntaxError") + " : " +
		Tools.getLocalizedString("Cause")+ " : " + cause;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	

}
