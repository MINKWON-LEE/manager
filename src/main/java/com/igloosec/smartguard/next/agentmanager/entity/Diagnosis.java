/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.utils.DIAGNOSIS.java
 * company : Mobigen
 * @author : Jong Seong Lee
 * created at : 2016. 2. 15.
 * description : 
 */

package com.igloosec.smartguard.next.agentmanager.entity;

public class Diagnosis {
	private String code;
	private String itemGroupName;
	private String itemName;
	private String itemGrade;
	private String standard;
	private String status;
	private String countermeasure;
	private String tip;
	private String result;

	// 2016.03.23 불가 사유 추가
	private String itemCokReason;


	private String assetCd;
	private String swType;
	private String swNm;
	private String swInfo;
	private String ipAddress;
	private String auditDay;
	private String hostNm;
	
	// 2016 .11.18 다중 진단 추가 정보
	private String swDir;
	private String swUser;
	private String swEtc;
	
	// 변경전 진단결과 추가 (홍순풍, 2017-03-08)
	private String orgItemResult;

	// 진단기준 추가
	private int govFlag;

	// 진단제외 추가
	private String exceptYn;
	
	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}



	/**
	 * @param code the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}



	/**
	 * @return the itemGroupName
	 */
	public String getItemGroupName() {
		return itemGroupName;
	}



	/**
	 * @param itemGroupName the itemGroupName to set
	 */
	public void setItemGroupName(String itemGroupName) {
		this.itemGroupName = itemGroupName;
	}



	/**
	 * @return the itemName
	 */
	public String getItemName() {
		return itemName;
	}



	/**
	 * @param itemName the itemName to set
	 */
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}



	/**
	 * @return the itemGrade
	 */
	public String getItemGrade() {
		return itemGrade;
	}



	/**
	 * @param itemGrade the itemGrade to set
	 */
	public void setItemGrade(String itemGrade) {
		this.itemGrade = itemGrade;
	}



	/**
	 * @return the standard
	 */
	public String getStandard() {
		return standard;
	}



	/**
	 * @param standard the standard to set
	 */
	public void setStandard(String standard) {
		this.standard = standard;
	}



	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}



	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}



	/**
	 * @return the countermeasure
	 */
	public String getCountermeasure() {
		return countermeasure;
	}



	/**
	 * @param countermeasure the countermeasure to set
	 */
	public void setCountermeasure(String countermeasure) {
		this.countermeasure = countermeasure;
	}



	/**
	 * @return the tip
	 */
	public String getTip() {
		return tip;
	}



	/**
	 * @param tip the tip to set
	 */
	public void setTip(String tip) {
		this.tip = tip;
	}



	/**
	 * @return the result
	 */
	public String getResult() {
		return result;
	}



	/**
	 * @param result the result to set
	 */
	public void setResult(String result) {
		this.result = result;
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
	 * @return the assetCd
	 */
	public String getAssetCd() {
		return assetCd;
	}



	/**
	 * @param assetCd the assetCd to set
	 */
	public void setAssetCd(String assetCd) {
		this.assetCd = assetCd;
	}



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
	 * @return the ipAddress
	 */
	public String getIpAddress() {
		return ipAddress;
	}



	/**
	 * @param ipAddress the ipAddress to set
	 */
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}



	/**
	 * @return the auditDay
	 */
	public String getAuditDay() {
		return auditDay;
	}



	/**
	 * @param auditDay the auditDay to set
	 */
	public void setAuditDay(String auditDay) {
		this.auditDay = auditDay;
	}



	/**
	 * @return the hostNm
	 */
	public String getHostNm() {
		return hostNm;
	}



	/**
	 * @param hostNm the hostNm to set
	 */
	public void setHostNm(String hostNm) {
		this.hostNm = hostNm;
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



	public String getOrgItemResult() {
		return orgItemResult;
	}



	public void setOrgItemResult(String orgItemResult) {
		this.orgItemResult = orgItemResult;
	}


	public int getGovFlag() {
		return govFlag;
	}

	public void setGovFlag(int govFlag) {
		this.govFlag = govFlag;
	}

	public String getExceptYn() {
		return exceptYn;
	}

	public void setExceptYn(String exceptYn) {
		this.exceptYn = exceptYn;
	}

	@Override
	public String toString() {
		return "Diagnosis [code=" + code + ", itemGroupName=" + itemGroupName
				+ ", itemName=" + itemName + ", itemGrade=" + itemGrade
				+ ", standard=" + standard + ", status=" + status
				+ ", countermeasure=" + countermeasure + ", tip=" + tip
				+ ", result=" + result + ", itemCokReason=" + itemCokReason
				+ "]";
	}

}
