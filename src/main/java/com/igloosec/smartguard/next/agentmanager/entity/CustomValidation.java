/**
 * project : AgentManager
 * package : com.mobigen.snet.agentmanager.entity
 * company : Mobigen
 * 
 * @author Hyeon-sik Jung
 * @Date   2017. 2. 22.
 * Description : 
 * 
 */
package com.igloosec.smartguard.next.agentmanager.entity;

/**
 * Project : AgentManager
 * Package : com.mobigen.snet.agentmanager.entity
 * Company : Mobigen
 * File    : CustomValidation.java
 *
 * @author Hyeon-sik Jung
 * @Date   2017. 2. 22.
 * Description : 
 * 
 */
public class CustomValidation {

	private int no;
	private boolean isPass;
	private String reason;
	
	
	public CustomValidation(int no, boolean pass, String reason) {
		this.no = no;
		this.isPass = pass;
		this.reason = reason;
	}


	public int getNo() {
		return no;
	}


	public void setNo(int no) {
		this.no = no;
	}


	public boolean isPass() {
		return isPass;
	}


	public void setPass(boolean isPass) {
		this.isPass = isPass;
	}


	public String getReason() {
		return reason;
	}


	public void setReason(String reason) {
		this.reason = reason;
	}
	
}
