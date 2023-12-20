/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.entity.SgwDBEntity.java
 * company : Mobigen
 * @author : Je Joong Lee
 * created at : 2016. 3. 17.
 * description : 
 */

package com.igloosec.smartguard.next.agentmanager.entity;

public class AssetMasterStatusDBEntity extends BaseDBEntity{

	private String masterIp; // SNET_ASSET_MASTER -> IP_ADDRESS
	/**
	 * 0 미등록
	 * 1 등록
	 */
	private int sgwRegi; //  SNET_ASSET_MASTER -> SGW_REGI
	/**
	 * 미응답(0), 
	 * Port 체크 응답(1)
	 */
	private int aliveChk; //  SNET_ASSET_MASTER -> ALIVE_CHK
	private String ipaddr;   // AUDIT_CONFIG_SG   -> IPADDR
	private String hostname; // AUDIT_CONFIG_SG
	private String ssh;		// AUDIT_CONFIG_SG
	private String telnet;	// AUDIT_CONFIG_SG
	private String sftp;	// AUDIT_CONFIG_SG
	private String ftp;		// AUDIT_CONFIG_SG
	private String window;	// AUDIT_CONFIG_SG
	/**
	 * @return the masterIp
	 */
	public String getMasterIp() {
		return masterIp;
	}
	/**
	 * @param masterIp the masterIp to set
	 */
	public void setMasterIp(String masterIp) {
		this.masterIp = masterIp;
	}
	/**
	 * @return the sgwRegi
	 */
	public int getSgwRegi() {
		return sgwRegi;
	}
	/**
	 * @param sgwRegi the sgwRegi to set
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
	 * @param aliveChk the aliveChk to set
	 */
	public void setAliveChk(int aliveChk) {
		this.aliveChk = aliveChk;
	}
	/**
	 * @return the ipaddr
	 */
	public String getIpaddr() {
		return ipaddr;
	}
	/**
	 * @param ipaddr the ipaddr to set
	 */
	public void setIpaddr(String ipaddr) {
		this.ipaddr = ipaddr;
	}
	/**
	 * @return the hostname
	 */
	public String getHostname() {
		return hostname;
	}
	/**
	 * @param hostname the hostname to set
	 */
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	/**
	 * @return the ssh
	 */
	public String getSsh() {
		return ssh;
	}
	/**
	 * @param ssh the ssh to set
	 */
	public void setSsh(String ssh) {
		this.ssh = ssh;
	}
	/**
	 * @return the telnet
	 */
	public String getTelnet() {
		return telnet;
	}
	/**
	 * @param telnet the telnet to set
	 */
	public void setTelnet(String telnet) {
		this.telnet = telnet;
	}
	/**
	 * @return the sftp
	 */
	public String getSftp() {
		return sftp;
	}
	/**
	 * @param sftp the sftp to set
	 */
	public void setSftp(String sftp) {
		this.sftp = sftp;
	}
	/**
	 * @return the ftp
	 */
	public String getFtp() {
		return ftp;
	}
	/**
	 * @param ftp the ftp to set
	 */
	public void setFtp(String ftp) {
		this.ftp = ftp;
	}
	/**
	 * @return the window
	 */
	public String getWindow() {
		return window;
	}
	/**
	 * @param window the window to set
	 */
	public void setWindow(String window) {
		this.window = window;
	}
}
