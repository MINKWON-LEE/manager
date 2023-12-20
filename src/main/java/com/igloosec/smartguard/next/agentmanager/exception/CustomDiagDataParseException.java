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
 * File    : CustomDiagDataParseException.java
 *
 * @author Hyeon-sik Jung
 * @Date   2017. 2. 22.
 * Description : 
 * 
 */
public class CustomDiagDataParseException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public CustomDiagDataParseException() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public CustomDiagDataParseException(String message, Throwable cause, boolean enableSuppression,
                                        boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public CustomDiagDataParseException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public CustomDiagDataParseException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public CustomDiagDataParseException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}
}
