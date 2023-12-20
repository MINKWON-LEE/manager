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
 * File    : CustonDiagProcessingException.java
 *
 * @author Hyeon-sik Jung
 * @Date   2017. 2. 22.
 * Description : 
 * 
 */
public class CustomDiagProcessingException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public CustomDiagProcessingException() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public CustomDiagProcessingException(String message, Throwable cause, boolean enableSuppression,
                                         boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public CustomDiagProcessingException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public CustomDiagProcessingException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public CustomDiagProcessingException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	
}
