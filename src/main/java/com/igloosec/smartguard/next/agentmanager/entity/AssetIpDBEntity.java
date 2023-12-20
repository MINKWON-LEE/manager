/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.services.AssetIpDBEntity.java
 * company : Mobigen
 * @author : Je Joong Lee
 * created at : 2016. 2. 26.
 * description : 
 */

package com.igloosec.smartguard.next.agentmanager.entity;

public class AssetIpDBEntity extends BaseDBEntity {
	private String ipAddress;
	private int ipRepresent;
	private int ipVersion;
	private int sgwRegi;
	private int userRegi;
	private String ifNm;
	private String ipV6Address;
	private String macAddress;
	private String defaultGw;

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
	 * @return the ipRepresent
	 */
	public int getIpRepresent() {
		return ipRepresent;
	}

	/**
	 * @param ipRepresent
	 *            the ipRepresent to set
	 */
	public void setIpRepresent(int ipRepresent) {
		this.ipRepresent = ipRepresent;
	}

	/**
	 * @return the ipVersion
	 */
	public int getIpVersion() {
		return ipVersion;
	}

	/**
	 * @param ipVersion
	 *            the ipVersion to set
	 */
	public void setIpVersion(int ipVersion) {
		this.ipVersion = ipVersion;
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


	public int getUserRegi() {
		return userRegi;
	}

	public void setUserRegi(int userRegi) {
		this.userRegi = userRegi;
	}

	public String getIfNm() {
		return ifNm;
	}

	public void setIfNm(String ifNm) {
		this.ifNm = ifNm;
	}

	public String getIpV6Address() {
		return ipV6Address;
	}

	public void setIpV6Address(String ipV6Address) {
		this.ipV6Address = ipV6Address;
	}

	public String getMacAddress() {
		return macAddress;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

	public String getDefaultGw() {
		return defaultGw;
	}

	public void setDefaultGw(String defaultGw) {
		this.defaultGw = defaultGw;
	}

	@Override
	public String toString() {
		return "AssetIpDBEntity [ipAddress=" + ipAddress + ", ipRepresent="
				+ ipRepresent + ", ipVersion=" + ipVersion + ", sgwRegi="
				+ sgwRegi + ", userRegi=" + userRegi
				+ ", ifNm=" + ifNm + ", ipV6Address=" + ipV6Address
				+ ", macAddress=" + macAddress + ", defaultGw=" + defaultGw + "]";
	}

}
