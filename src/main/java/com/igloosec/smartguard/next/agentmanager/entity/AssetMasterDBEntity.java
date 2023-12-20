/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.entity.AssetMasterDBEntity.java
 * company : Mobigen
 * @author : Je Joong Lee
 * created at : 2016. 2. 26.
 * description : 
 */

package com.igloosec.smartguard.next.agentmanager.entity;

import jodd.util.StringUtil;

public class AssetMasterDBEntity extends BaseDBEntity {

	private String agentCd;
	private String branchId;
	private String branchNm;
	private String teamId;
	private String teamNm;
	private String ipAddress;
	private String hostNm;
	private String auditDay;
	private String getDay;
	private float auditRate;
	private float auditRateFirewall;
	private int sgwRegi;
	private int aliveChk;
	private String svrRoomId;
	private int personalData;
	private int govFlag;
	private String vendor;
	private String serial;
	private String cpu;
	private String mem;
	private String disk;

	private String dgUserId;

	private String dgUserNm;

	
	private String assetRmk;      // cloud InstanceId;

	/**
	 * @return the agentCd
	 */
	public String getAgentCd() {
		return agentCd;
	}

	/**
	 * @param agentCd
	 *            the agentCd to set
	 */
	public void setAgentCd(String agentCd) {
		this.agentCd = agentCd;
	}

	/**
	 * @return the branchId
	 */
	public String getBranchId() {
		return branchId;
	}

	/**
	 * @param branchId
	 *            the branchId to set
	 */
	public void setBranchId(String branchId) {
		this.branchId = branchId;
	}

	/**
	 * @return the branchNm
	 */
	public String getBranchNm() {
		return branchNm;
	}

	/**
	 * @param branchNm
	 *            the branchNm to set
	 */
	public void setBranchNm(String branchNm) {
		this.branchNm = branchNm;
	}

	/**
	 * @return the teamId
	 */
	public String getTeamId() {
		return teamId;
	}

	/**
	 * @param teamId
	 *            the teamId to set
	 */
	public void setTeamId(String teamId) {
		this.teamId = teamId;
	}

	/**
	 * @return the teamNm
	 */
	public String getTeamNm() {
		return teamNm;
	}

	/**
	 * @param teamNm
	 *            the teamNm to set
	 */
	public void setTeamNm(String teamNm) {
		this.teamNm = teamNm;
	}

	/**
	 * @return the ipAddress
	 */
	public String getIpAddress() {
		return ipAddress;
	}

	/**
	 * @param ipAddress
	 *            the ipAddress to set
	 */
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	/**
	 * @return the hostNm
	 */
	public String getHostNm() {
		return hostNm;
	}

	/**
	 * @param hostNm
	 *            the hostNm to set
	 */
	public void setHostNm(String hostNm) {
		this.hostNm = hostNm;
	}

	/**
	 * @return the auditDay
	 */
	public String getAuditDay() {
		return auditDay;
	}

	/**
	 * @param auditDay
	 *            the auditDay to set
	 */
	public void setAuditDay(String auditDay) {
		this.auditDay = auditDay;
	}

	/**
	 * @return the sgwRegi
	 */
	public int getSgwRegi() {
		return sgwRegi;
	}

	/**
	 * @param sgwRegi
	 *            the sgwRegi to set
	 */
	public void setSgwRegi(int sgwRegi) {
		this.sgwRegi = sgwRegi;
	}

	/**
	 * @return the aliveChk
	 */
	public int getAliveChk() {
		return aliveChk;
	}

	/**
	 * @param aliveChk
	 *            the aliveChk to set
	 */
	public void setAliveChk(int aliveChk) {
		this.aliveChk = aliveChk;
	}

	/**
	 * @return the svrRoomId
	 */
	public String getSvrRoomId() {
		return svrRoomId;
	}

	/**
	 * @param svrRoomId
	 *            the svrRoomId to set
	 */
	public void setSvrRoomId(String svrRoomId) {
		this.svrRoomId = svrRoomId;
	}

	/**
	 * @return the personalData
	 */
	public int getPersonalData() {
		return personalData;
	}

	/**
	 * @param personalData
	 *            the personalData to set
	 */
	public void setPersonalData(int personalData) {
		this.personalData = personalData;
	}

	/**
	 * @return the govFlag
	 */
	public int getGovFlag() {
		return govFlag;
	}

	/**
	 * @param govFlag the govFlag to set
	 */
	public void setGovFlag(int govFlag) {
		this.govFlag = govFlag;
	}

	/**
	 * @return the auditRate
	 */
	public float getAuditRate() {
		return auditRate;
	}

	/**
	 * @param auditRate
	 *            the auditRate to set
	 */
	public void setAuditRate(float auditRate) {
		this.auditRate = auditRate;
	}
	
	/**
	 * @return the auditRate firewall check
	 */
	public float getAuditRateFirewall() {
		return auditRateFirewall;
	}

	/**
	 * @param auditRate firewall check
	 *            the auditRate firewall check to set
	 */
	public void setAuditRateFirewall(float auditRateFirewall) {
		this.auditRateFirewall = auditRateFirewall;
	}

	/**
	 * @return the getDay
	 */
	public String getGetDay() {
		return getDay;
	}

	/**
	 * @param getDay the getDay to set
	 */
	public void setGetDay(String getDay) {
		this.getDay = getDay;
	}


	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
		if (StringUtil.isEmpty(this.vendor)) {
			this.vendor = "-";
		}
	}

	public String getSerial() {
		return serial;
	}

	public void setSerial(String serial) {
		this.serial = serial;
		if (StringUtil.isEmpty(this.serial)) {
			this.serial = "-";
		}
	}

	public String getCpu() {
		return cpu;
	}

	public void setCpu(String cpu) {
		this.cpu = cpu;
		if (StringUtil.isEmpty(this.cpu)) {
			this.cpu = "-";
		}
	}

	public String getMem() {
		return mem;
	}

	public void setMem(String mem) {
		this.mem = mem;
		if (StringUtil.isEmpty(this.mem)) {
			this.mem = "-";
		}
	}

	public String getDisk() {
		return disk;
	}

	public void setDisk(String disk) {
		this.disk = disk;
		if (StringUtil.isEmpty(this.disk)) {
			this.disk = "-";
		}
	}

	public String getDgUserId() {
		return dgUserId;
	}

	public void setDgUserId(String dgUserId) {
		this.dgUserId = dgUserId;
	}

	public String getDgUserNm() {
		return dgUserNm;
	}

	public void setDgUserNm(String dgUserNm) {
		this.dgUserNm = dgUserNm;
	}

	public String getAssetRmk() { return assetRmk; }

	public void setAssetRmk(String assetRmk) { this.assetRmk = assetRmk; }

	@Override
	public String toString() {
		return "AssetMasterDBEntity [agentCd=" + agentCd + ", branchId="
				+ branchId + ", branchNm=" + branchNm + ", teamId=" + teamId
				+ ", teamNm=" + teamNm + ", ipAddress=" + ipAddress
				+ ", hostNm=" + hostNm + ", auditDay=" + auditDay
				+ ", auditRate=" + auditRate + ", auditRateFirewall=" + auditRateFirewall + ", sgwRegi=" + sgwRegi
				+ ", aliveChk=" + aliveChk + ", svrRoomId=" + svrRoomId
				+ ", personalData=" + personalData + ", govFlag=" + govFlag
				+ ", vendor=" + vendor + ", serial=" + serial
				+ ", cpu=" + cpu + ", mem=" + mem
				+ ", disk=" + disk
				+ ", dgUserId=" + dgUserId
				+ ", dgUserNm=" + dgUserNm
				+ ", assetRmk=" + assetRmk
				+ "]";
	}

}
