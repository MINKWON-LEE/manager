/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.entity
 * company : Mobigen
 * @author : Hyeon-sik Jung
 * created at : 2016. 6. 3.
 * description : 
 */
package com.igloosec.smartguard.next.agentmanager.entity;

/**
 * @author Hyeon-sik Jung
 *
 */
public class AgentStatus extends BaseDBEntity{

	private String agentCd;
	private Integer agentStatus;
	private String agentStatusDesc;
	/**
	 * @return the agentCd
	 */
	public String getAgentCd() {
		return agentCd;
	}
	/**
	 * @param agentCd the agentCd to set
	 */
	public void setAgentCd(String agentCd) {
		this.agentCd = agentCd;
	}
	/**
	 * @return the agentStatus
	 */
	public Integer getAgentStatus() {
		return agentStatus;
	}
	/**
	 * @param agentStatus the agentStatus to set
	 */
	public void setAgentStatus(Integer agentStatus) {
		this.agentStatus = agentStatus;
	}
	/**
	 * @return the agentStatusDesc
	 */
	public String getAgentStatusDesc() {
		return agentStatusDesc;
	}
	/**
	 * @param agentStatusDesc the agentStatusDesc to set
	 */
	public void setAgentStatusDesc(String agentStatusDesc) {
		this.agentStatusDesc = agentStatusDesc;
	}
}
