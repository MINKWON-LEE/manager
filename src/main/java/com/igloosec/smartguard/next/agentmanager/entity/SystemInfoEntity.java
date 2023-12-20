/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.entity.SystemInfoEntity.java
 * company : Mobigen
 * @author : Je Joong Lee
 * created at : 2016. 2. 29.
 * description : 
 */

package com.igloosec.smartguard.next.agentmanager.entity;

import java.util.List;

public class SystemInfoEntity {
	private String hostNm;
	private String asset;
	private String script;
	private String date;
	private String time;
	private String ipAddress;
	private List<String> listipAddress;
	private String swType;
	private String swNm;
	private String swInfo;
	private String swDir;
	private String swUser;
	private String swEtc;
	private String prgNm;

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
	 * @return the asset
	 */
	public String getAsset() {
		return asset;
	}

	/**
	 * @param asset
	 *            the asset to set
	 */
	public void setAsset(String asset) {
		this.asset = asset;
	}

	/**
	 * @return the script
	 */
	public String getScript() {
		return script;
	}

	/**
	 * @param script
	 *            the script to set
	 */
	public void setScript(String script) {
		this.script = script;
	}

	/**
	 * @return the date
	 */
	public String getDate() {
		return date;
	}

	/**
	 * @param date
	 *            the date to set
	 */
	public void setDate(String date) {
		this.date = date;
	}

	/**
	 * @return the time
	 */
	public String getTime() {
		return time;
	}

	/**
	 * @param time
	 *            the time to set
	 */
	public void setTime(String time) {
		this.time = time;
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
	 * @return the listipAddress
	 */
	public List<String> getListipAddress() {
		return listipAddress;
	}

	/**
	 * @param listipAddress
	 *            the listipAddress to set
	 */
	public void setListipAddress(List<String> listipAddress) {
		this.listipAddress = listipAddress;
	}

	/**
	 * @return the swType
	 */
	public String getSwType() {
		return swType;
	}

	/**
	 * @param swType
	 *            the swType to set
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
	 * @param swNm
	 *            the swNm to set
	 */
	public void setSwNm(String swNm) {
		this.swNm = swNm;
	}

	/**
	 * @return the swInfo
	 */
	public String getSwInfo() {
		return swInfo;
	}

	/**
	 * @param swInfo
	 *            the swInfo to set
	 */
	public void setSwInfo(String swInfo) {
		this.swInfo = swInfo;
	}

	public String getSwDir() {
		return swDir;
	}

	public void setSwDir(String swDir) {
		this.swDir = swDir;
	}

	public String getSwUser() {
		return swUser;
	}

	public void setSwUser(String swUser) {
		this.swUser = swUser;
	}

	public String getSwEtc() {
		return swEtc;
	}

	public void setSwEtc(String swEtc) {
		this.swEtc = swEtc;
	}

	public String getPrgNm() {
		return prgNm;
	}

	public void setPrgNm(String prgNm) {
		this.prgNm = prgNm;
	}

	@Override
	public String toString() {
		return "SystemInfoEntity [hostNm=" + hostNm + ", asset=" + asset
				+ ", script=" + script + ", date=" + date + ", time=" + time
				+ ", ipAddress=" + ipAddress + ", listipAddress="
				+ listipAddress + ", swType=" + swType + ", swNm=" + swNm
				+ ", swInfo=" + swInfo + "]";
	}

}
