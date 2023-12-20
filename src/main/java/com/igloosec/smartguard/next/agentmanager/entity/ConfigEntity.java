/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.entity
 * company : Mobigen
 * @author : Hyeon-sik Jung
 * created at : 2016. 4. 12.
 * description : 
 */
package com.igloosec.smartguard.next.agentmanager.entity;

/**
 * @author Hyeon-sik Jung
 *
 */
public class ConfigEntity {

	private String name;
	private String value;
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
}
