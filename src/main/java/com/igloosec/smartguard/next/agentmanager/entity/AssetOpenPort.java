/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.entity.AssetOpenPort.java
 * company : Mobigen
 * @author : Je Joong Lee
 * created at : 2016. 3. 18.
 * description : 
 */

package com.igloosec.smartguard.next.agentmanager.entity;

public class AssetOpenPort extends BaseDBEntity {
	private Integer ipVersion;
	private Integer openType;
	private Integer openPort;
	private Integer processId;
	private String processNm;
	private String cdate;
	/**
	 * @return the ipVersion
	 */
	public Integer getIpVersion() {
		return ipVersion;
	}
	/**
	 * @param ipVersion the ipVersion to set
	 */
	public void setIpVersion(Integer ipVersion) {
		this.ipVersion = ipVersion;
	}
	/**
	 * @return the openType
	 */
	public Integer getOpenType() {
		return openType;
	}
	/**
	 * @param openType the openType to set
	 */
	public void setOpenType(Integer openType) {
		this.openType = openType;
	}
	/**
	 * @return the openPort
	 */
	public Integer getOpenPort() {
		return openPort;
	}
	/**
	 * @param openPort the openPort to set
	 */
	public void setOpenPort(Integer openPort) {
		this.openPort = openPort;
	}
	/**
	 * @return the processId
	 */
	public Integer getProcessId() {
		return processId;
	}
	/**
	 * @param processId the processId to set
	 */
	public void setProcessId(Integer processId) {
		this.processId = processId;
	}
	/**
	 * @return the processNm
	 */
	public String getProcessNm() {
		return processNm;
	}
	/**
	 * @param processNm the processNm to set
	 */
	public void setProcessNm(String processNm) {
		this.processNm = processNm;
	}
	
	/**
	 * @return the cdate
	 */
	public String getCdate() {
		return cdate;
	}
	/**
	 * @param cdate the cdate to set
	 */
	public void setCdate(String cdate) {
		this.cdate = cdate;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "AssetOpenPort [ipVersion=" + ipVersion + ", openType=" + openType + ", openPort=" + openPort
				+ ", processId=" + processId + ", processNm=" + processNm + ", cdate=" + cdate + "]";
	}
	
}
