/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.services.GscriptResultEntity.java
 * company : Mobigen
 * @author : Je Joong Lee
 * created at : 2016. 2. 26.
 * description : 
 */

package com.igloosec.smartguard.next.agentmanager.entity;

import java.util.List;

public class GscriptResultEntity extends BaseDBEntity implements Cloneable {
	private String managerCd;
	
	// 2016.03.15 추가
	private int osArch;
	
	private AssetMasterDBEntity assetMasterDBEntity;
	private ConfigUserViewDBEntity userViewDBEntity;
	private List<SwAuditDayDBEntity> listSwAuditDay;
	private List<AssetIpDBEntity> listAssetIp;

	// 2016.03.18 추가 수정
	private List<AssetOpenPort> listassetOpenPort;
	
	//2016.03.30 추가
	private String cdate;
	
	// 2016.03.31 추가 
	private String ConnectLog;
	
	// 2016.11.29 Added
	private String connectIpAddress;

	private String shadow;
	
	private String cOtp;
	
	
	/**
	 * @return the managerCd
	 */
	public String getManagerCd() {
		return managerCd;
	}

	/**
	 * @param managerCd
	 *            the managerCd to set
	 */
	public void setManagerCd(String managerCd) {
		this.managerCd = managerCd;
	}

	/**
	 * @return the assetMasterDBEntity
	 */
	public AssetMasterDBEntity getAssetMasterDBEntity() {
		return assetMasterDBEntity;
	}

	/**
	 * @param assetMasterDBEntity
	 *            the assetMasterDBEntity to set
	 */
	public void setAssetMasterDBEntity(
			AssetMasterDBEntity assetMasterDBEntity) {
		this.assetMasterDBEntity = assetMasterDBEntity;
	}

	/**
	 * @return the userViewDBEntity
	 */
	public ConfigUserViewDBEntity getUserViewDBEntity() {
		return userViewDBEntity;
	}

	/**
	 * @param userViewDBEntity
	 *            the userViewDBEntity to set
	 */
	public void setUserViewDBEntity(ConfigUserViewDBEntity userViewDBEntity) {
		this.userViewDBEntity = userViewDBEntity;
	}

	/**
	 * @return the listSwAuditDay
	 */
	public List<SwAuditDayDBEntity> getListSwAuditDay() {
		return listSwAuditDay;
	}

	/**
	 * @param listSwAuditDay
	 *            the listSwAuditDay to set
	 */
	public void setListSwAuditDay(List<SwAuditDayDBEntity> listSwAuditDay) {
		this.listSwAuditDay = listSwAuditDay;
	}

	/**
	 * @return the listAssetIp
	 */
	public List<AssetIpDBEntity> getListAssetIp() {
		return listAssetIp;
	}

	/**
	 * @param listAssetIp
	 *            the listAssetIp to set
	 */
	public void setListAssetIp(List<AssetIpDBEntity> listAssetIp) {
		this.listAssetIp = listAssetIp;
	}

	/**
	 * @return the osArch
	 */
	public int getOsArch() {
		return osArch;
	}

	/**
	 * @param osArch
	 *            the osArch to set
	 */
	public void setOsArch(int osArch) {
		this.osArch = osArch;
	}

	/**
	 * @return the listassetOpenPort
	 */
	public List<AssetOpenPort> getListassetOpenPort() {
		return listassetOpenPort;
	}

	/**
	 * @param listassetOpenPort
	 *            the listassetOpenPort to set
	 */
	public void setListassetOpenPort(List<AssetOpenPort> listassetOpenPort) {
		this.listassetOpenPort = listassetOpenPort;
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
	
	/**
	 * @return the connectLog
	 */
	public String getConnectLog() {
		return ConnectLog;
	}

	/**
	 * @param connectLog the connectLog to set
	 */
	public void setConnectLog(String connectLog) {
		ConnectLog = connectLog;
	}
	public String getConnectIpAddress() {
		return connectIpAddress;
	}

	public void setConnectIpAddress(String connectIpAddress) {
		this.connectIpAddress = connectIpAddress;
	}

	public String getShadow() {
		return shadow;
	}

	public void setShadow(String shadow) {
		this.shadow = shadow;
	}

	public String getcOtp() {
		return cOtp;
	}

	public void setcOtp(String cOtp) {
		this.cOtp = cOtp;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "GscriptResultEntity [managerCd=" + managerCd + ", osArch=" + osArch + ", assetMasterDBEntity="
				+ assetMasterDBEntity + ", userViewDBEntity=" + userViewDBEntity + ", listSwAuditDay=" + listSwAuditDay
				+ ", listAssetIp=" + listAssetIp + ", listassetOpenPort=" + listassetOpenPort + ", cdate=" + cdate
				+ ", ConnectLog=" + ConnectLog + "]";
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
