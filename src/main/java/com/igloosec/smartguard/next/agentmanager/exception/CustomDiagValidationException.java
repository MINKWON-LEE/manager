/**
 * project : AgentManager
 * package : com.mobigen.snet.agentmanager.exception
 * company : Mobigen
 * 
 * @author Hyeon-sik Jung
 * @Date   2017. 2. 22.
 * Description : 
 * 
 */
package com.igloosec.smartguard.next.agentmanager.exception;

/**
 * Project : AgentManager
 * Package : com.mobigen.snet.agentmanager.exception
 * Company : Mobigen
 * File    : CustomDiagValidationException.java
 *
 * @author Hyeon-sik Jung
 * @Date   2017. 2. 22.
 * Description : 
 * 
 */
public class CustomDiagValidationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public CustomDiagValidationException() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public CustomDiagValidationException(String message, Throwable cause, boolean enableSuppression,
                                         boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public CustomDiagValidationException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public CustomDiagValidationException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public CustomDiagValidationException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}
	
	

}
