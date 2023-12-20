/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.entity
 * company : Mobigen
 * @author : Hyeon-sik Jung
 * created at : 2016. 4. 22.
 * description : 
 */
package com.igloosec.smartguard.next.agentmanager.entity;

/**
 * @author Hyeon-sik Jung
 *
 */
public class ReportEntity {

	private String branchId;
	
	private String teamId;

	private String hostNm;
	
	private String ipAddress;
	
	
	private String swType;
	
	private String swNm;

	/**
	 * @return the branchId
	 */
	public String getBranchId() {
		return branchId;
	}

	/**
	 * @param branchId the branchId to set
	 */
	public void setBranchId(String branchId) {
		this.branchId = branchId;
	}

	/**
	 * @return the teamId
	 */
	public String getTeamId() {
		return teamId;
	}

	/**
	 * @param teamId the teamId to set
	 */
	public void setTeamId(String teamId) {
		this.teamId = teamId;
	}

	/**
	 * @return the hostNm
	 */
	public String getHostNm() {
		return hostNm;
	}

	/**
	 * @param hostNm the hostNm to set
	 */
	public void setHostNm(String hostNm) {
		this.hostNm = hostNm;
	}

	/**
	 * @return the ipAddress
	 */
	public String getIpAddress() {
		return ipAddress;
	}

	/**
	 * @param ipAddress the ipAddress to set
	 */
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	/**
	 * @return the swType
	 */
	public String getSwType() {
		return swType;
	}

	/**
	 * @param swType the swType to set
	 */
	public void setSwType(String swType) {
		this.swType = swType;
	}

	/**
	 * @return the swNm
	 */
	public String getSwNm() {
		return swNm;
	}

	/**
	 * @param swNm the swNm to set
	 */
	public void setSwNm(String swNm) {
		this.swNm = swNm;
	}
}
