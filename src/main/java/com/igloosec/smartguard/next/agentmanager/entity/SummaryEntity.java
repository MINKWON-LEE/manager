/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.entity.SummaryEntity.java
 * company : Mobigen
 * @author : Je Joong Lee
 * created at : 2016. 2. 29.
 * description : 
 */

package com.igloosec.smartguard.next.agentmanager.entity;

public class SummaryEntity {
	private String vulCount;
	private String score;
	private String vulItemList;
	private String nochechItemList;
	private String manualCheckItemList;

	/**
	 * @return the vulCount
	 */
	public String getVulCount() {
		return vulCount;
	}

	/**
	 * @param vulCount
	 *            the vulCount to set
	 */
	public void setVulCount(String vulCount) {
		this.vulCount = vulCount;
	}

	/**
	 * @return the score
	 */
	public String getScore() {
		return score;
	}

	/**
	 * @param score
	 *            the score to set
	 */
	public void setScore(String score) {
		this.score = score;
	}

	/**
	 * @return the vulItemList
	 */
	public String getVulItemList() {
		return vulItemList;
	}

	/**
	 * @param vulItemList
	 *            the vulItemList to set
	 */
	public void setVulItemList(String vulItemList) {
		this.vulItemList = vulItemList;
	}

	/**
	 * @return the nochechItemList
	 */
	public String getNochechItemList() {
		return nochechItemList;
	}

	/**
	 * @param nochechItemList
	 *            the nochechItemList to set
	 */
	public void setNochechItemList(String nochechItemList) {
		this.nochechItemList = nochechItemList;
	}

	/**
	 * @return the manualCheckItemList
	 */
	public String getManualCheckItemList() {
		return manualCheckItemList;
	}

	/**
	 * @param manualCheckItemList
	 *            the manualCheckItemList to set
	 */
	public void setManualCheckItemList(String manualCheckItemList) {
		this.manualCheckItemList = manualCheckItemList;
	}

	@Override
	public String toString() {
		return "SummaryEntity [vulCount=" + vulCount + ", score=" + score
				+ ", vulItemList=" + vulItemList + ", nochechItemList="
				+ nochechItemList + ", manualCheckItemList="
				+ manualCheckItemList + "]";
	}

}
