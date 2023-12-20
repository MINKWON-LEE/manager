/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.entity.ConfigUserViewDBEntity.java
 * company : Mobigen
 * @author : Je Joong Lee
 * created at : 2016. 2. 26.
 * description : 
 */

package com.igloosec.smartguard.next.agentmanager.entity;

public class ConfigUserViewDBEntity {
	private String userId;
	private String managerCd;
	private String userNm;
	private String userMs;
	private String userPw;
	private String userMail;
	private String userAuth;
	private String branchId;
	private String branchNm;
	private String teamId;
	private String teamNm;

	/**
	 * @return the userId
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * @param userId
	 *            the userId to set
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}

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
	 * @return the userNm
	 */
	public String getUserNm() {
		return userNm;
	}

	/**
	 * @param userNm
	 *            the userNm to set
	 */
	public void setUserNm(String userNm) {
		this.userNm = userNm;
	}

	/**
	 * @return the userMs
	 */
	public String getUserMs() {
		return userMs;
	}

	/**
	 * @param userMs
	 *            the userMs to set
	 */
	public void setUserMs(String userMs) {
		this.userMs = userMs;
	}

	/**
	 * @return the userPw
	 */
	public String getUserPw() {
		return userPw;
	}

	/**
	 * @param userPw
	 *            the userPw to set
	 */
	public void setUserPw(String userPw) {
		this.userPw = userPw;
	}

	/**
	 * @return the userMail
	 */
	public String getUserMail() {
		return userMail;
	}

	/**
	 * @param userMail
	 *            the userMail to set
	 */
	public void setUserMail(String userMail) {
		this.userMail = userMail;
	}

	/**
	 * @return the userAuth
	 */
	public String getUserAuth() {
		return userAuth;
	}

	/**
	 * @param userAuth
	 *            the userAuth to set
	 */
	public void setUserAuth(String userAuth) {
		this.userAuth = userAuth;
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

	@Override
	public String toString() {
		return "ConfigUserViewDBEntity [userId=" + userId + ", managerCd="
				+ managerCd + ", userNm=" + userNm + ", userMs=" + userMs
				+ ", userPw=" + userPw + ", userMail=" + userMail
				+ ", userAuth=" + userAuth + ", branchId=" + branchId
				+ ", branchNm=" + branchNm + ", teamId=" + teamId + ", teamNm="
				+ teamNm + "]";
	}

}
