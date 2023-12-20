/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.exception.GetScriptException.java
 * company : Mobigen
 * @author : Oh su jin
 * created at : 2016. 4. 3.
 * description :
 */
package com.igloosec.smartguard.next.agentmanager.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetScriptException extends Exception {

	private static final long serialVersionUID = 7735107398393243439L;

	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	private String message;

	public GetScriptException(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	
	
}
