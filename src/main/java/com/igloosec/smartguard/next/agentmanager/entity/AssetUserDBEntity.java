/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.entity.AssetUserDBEntity.java
 * company : Mobigen
 * @author : Je Joong Lee
 * created at : 2016. 2. 26.
 * description : 
 */

package com.igloosec.smartguard.next.agentmanager.entity;

public class AssetUserDBEntity extends BaseDBEntity {
	private String userId;
	private String userType;
	private String teamId;
	private String teamNm;
	private String userNm;
	private String userMs;
	private String userMail;

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
	 * @return the userType
	 */
	public String getUserType() {
		return userType;
	}

	/**
	 * @param userType
	 *            the userType to set
	 */
	public void setUserType(String userType) {
		this.userType = userType;
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

	@Override
	public String toString() {
		return "AssetUserDBEntity [userId=" + userId + ", userType=" + userType
				+ ", teamId=" + teamId + ", teamNm=" + teamNm + ", userNm="
				+ userNm + ", userMs=" + userMs + ", userMail=" + userMail
				+ "]";
	}

}
