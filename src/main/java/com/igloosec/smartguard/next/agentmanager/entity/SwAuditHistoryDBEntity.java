/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.entity.SwAuditHistoryDBEntity.java
 * company : Mobigen
 * @author : Je Joong Lee
 * created at : 2016. 3. 4.
 * description : 
 */

package com.igloosec.smartguard.next.agentmanager.entity;

public class SwAuditHistoryDBEntity extends BaseDBEntity {
	private int adResultNa;
	private int adResultPass;
	private int adWeightNok;
	private int adWeightTotal;
	private int adWeightOk;
	private double auditRate;
	private int adResultOk;
	private int adResultNok;
	private int adResultReq;
	private int adWeightNa;
	private int adWeightReq;
	private int adWeightPass;
	private String teamId;
	private String swType;
	private String teamNm;
	private String swInfo;
	private String auditFileCd;
	private String hostNm;
	private String ipAddress;
	private String swNm;
	private String assetCd;
	private String branchId;
	private String auditType;
	private String branchNm;
	private String auditDay;
	private String userId;
	private String userNm;

	/*
	 * 2016.11.18
	 * 다중 WEB, WAS, DB 추가 정보
	 */
	private String swDir="-";
	private String swUser="-";
	private String swEtc="-";
	
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
	 * @return the adResultNa
	 */
	public int getAdResultNa() {
		return adResultNa;
	}

	/**
	 * @param adResultNa
	 *            the adResultNa to set
	 */
	public void setAdResultNa(int adResultNa) {
		this.adResultNa = adResultNa;
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
	 * @return the adResultPass
	 */
	public int getAdResultPass() {
		return adResultPass;
	}

	/**
	 * @param adResultPass
	 *            the adResultPass to set
	 */
	public void setAdResultPass(int adResultPass) {
		this.adResultPass = adResultPass;
	}

	/**
	 * @return the adWeightNok
	 */
	public int getAdWeightNok() {
		return adWeightNok;
	}

	/**
	 * @param adWeightNok
	 *            the adWeightNok to set
	 */
	public void setAdWeightNok(int adWeightNok) {
		this.adWeightNok = adWeightNok;
	}

	/**
	 * @return the adWeightReq
	 */
	public int getAdWeightReq() {
		return adWeightReq;
	}

	/**
	 * @param adWeightReq the adWeightReq to set
	 */
	public void setAdWeightReq(int adWeightReq) {
		this.adWeightReq = adWeightReq;
	}

	/**
	 * @return the adWeightTotal
	 */
	public int getAdWeightTotal() {
		return adWeightTotal;
	}

	/**
	 * @param adWeightTotal
	 *            the adWeightTotal to set
	 */
	public void setAdWeightTotal(int adWeightTotal) {
		this.adWeightTotal = adWeightTotal;
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

	/**
	 * @return the auditFileCd
	 */
	public String getAuditFileCd() {
		return auditFileCd;
	}

	/**
	 * @param auditFileCd
	 *            the auditFileCd to set
	 */
	public void setAuditFileCd(String auditFileCd) {
		this.auditFileCd = auditFileCd;
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
	 * @return the adWeightOk
	 */
	public int getAdWeightOk() {
		return adWeightOk;
	}

	/**
	 * @param adWeightOk
	 *            the adWeightOk to set
	 */
	public void setAdWeightOk(int adWeightOk) {
		this.adWeightOk = adWeightOk;
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
	 * @return the auditRate
	 */
	public double getAuditRate() {
		return auditRate;
	}

	/**
	 * @param auditRate
	 *            the auditRate to set
	 */
	public void setAuditRate(double auditRate) {
		this.auditRate = auditRate;
	}

	/**
	 * @return the assetCd
	 */
	public String getAssetCd() {
		return assetCd;
	}

	/**
	 * @param assetCd
	 *            the assetCd to set
	 */
	public void setAssetCd(String assetCd) {
		this.assetCd = assetCd;
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
	 * @return the adResultOk
	 */
	public int getAdResultOk() {
		return adResultOk;
	}

	/**
	 * @param adResultOk
	 *            the adResultOk to set
	 */
	public void setAdResultOk(int adResultOk) {
		this.adResultOk = adResultOk;
	}

	/**
	 * @return the adResultNok
	 */
	public int getAdResultNok() {
		return adResultNok;
	}

	/**
	 * @param adResultNok
	 *            the adResultNok to set
	 */
	public void setAdResultNok(int adResultNok) {
		this.adResultNok = adResultNok;
	}

	
	/**
	 * @return the adResultReq
	 */
	public int getAdResultReq() {
		return adResultReq;
	}

	/**
	 * @param adResultReq the adResultReq to set
	 */
	public void setAdResultReq(int adResultReq) {
		this.adResultReq = adResultReq;
	}

	/**
	 * @return the adWeightNa
	 */
	public int getAdWeightNa() {
		return adWeightNa;
	}

	/**
	 * @param adWeightNa
	 *            the adWeightNa to set
	 */
	public void setAdWeightNa(int adWeightNa) {
		this.adWeightNa = adWeightNa;
	}

	/**
	 * @return the adWeightPass
	 */
	public int getAdWeightPass() {
		return adWeightPass;
	}

	/**
	 * @param adWeightPass
	 *            the adWeightPass to set
	 */
	public void setAdWeightPass(int adWeightPass) {
		this.adWeightPass = adWeightPass;
	}

	/**
	 * @return the auditType
	 */
	public String getAuditType() {
		return auditType;
	}

	/**
	 * @param auditType
	 *            the auditType to set
	 */
	public void setAuditType(String auditType) {
		this.auditType = auditType;
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
	 * @return the userId
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * @param userId the userId to set
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}

	/**
	 * @return the userNm
	 */
	public String getUserNm() {
		return userNm;
	}

	/**
	 * @param userNm the userNm to set
	 */
	public void setUserNm(String userNm) {
		this.userNm = userNm;
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

	@Override
	public String toString() {
		return "SwAuditHistoryDBEntity [adResultNa=" + adResultNa
				+ ", adResultPass=" + adResultPass + ", adWeightNok="
				+ adWeightNok + ", adWeightTotal=" + adWeightTotal
				+ ", adWeightOk=" + adWeightOk + ", auditRate=" + auditRate
				+ ", adResultOk=" + adResultOk + ", adResultNok=" + adResultNok
				+ ", adWeightNa=" + adWeightNa + ", adWeightPass="
				+ adWeightPass + ", teamId=" + teamId + ", swType=" + swType
				+ ", teamNm=" + teamNm + ", swInfo=" + swInfo + ", auditFileCd="
				+ auditFileCd + ", hostNm=" + hostNm + ", ipAddress="
				+ ipAddress + ", swNm=" + swNm + ", assetCd=" + assetCd
				+ ", branchId=" + branchId + ", auditType=" + auditType
				+ ", branchNm=" + branchNm + ", auditDay=" + auditDay + "]";
	}

}