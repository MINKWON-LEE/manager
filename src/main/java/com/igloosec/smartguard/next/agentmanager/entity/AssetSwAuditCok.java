/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.entity.AssetSwAuditCok.java
 * company : Mobigen
 * @author : Je Joong Lee
 * created at : 2016. 3. 23.
 * description : 
 */

package com.igloosec.smartguard.next.agentmanager.entity;

public class AssetSwAuditCok extends BaseDBEntity {
	private String swType;
	private String swNm;
	private String swInfo;
	private String diagnosisCd;
	private String itemCokReason;
	private String actionItemResult;
	/**
	 * @return the swType
	 */
	public String getSwType() {
		return swType;
	}
	/**
	 * @param swType the swType to set
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
	 * @param swNm the swNm to set
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
	 * @param swInfo the swInfo to set
	 */
	public void setSwInfo(String swInfo) {
		this.swInfo = swInfo;
	}
	/**
	 * @return the diagnosisCd
	 */
	public String getDiagnosisCd() {
		return diagnosisCd;
	}
	/**
	 * @param diagnosisCd the diagnosisCd to set
	 */
	public void setDiagnosisCd(String diagnosisCd) {
		this.diagnosisCd = diagnosisCd;
	}
	/**
	 * @return the itemCokReason
	 */
	public String getItemCokReason() {
		return itemCokReason;
	}
	/**
	 * @param itemCokReason the itemCokReason to set
	 */
	public void setItemCokReason(String itemCokReason) {
		this.itemCokReason = itemCokReason;
	}
	/**
	 * @return the actionItemResult
	 */
	public String getActionItemResult() {
		return actionItemResult;
	}
	/**
	 * @param actionItemResult the actionItemResult to set
	 */
	public void setActionItemResult(String actionItemResult) {
		this.actionItemResult = actionItemResult;
	}
}
