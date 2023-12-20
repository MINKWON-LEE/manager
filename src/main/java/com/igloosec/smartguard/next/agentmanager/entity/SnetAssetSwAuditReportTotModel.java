package com.igloosec.smartguard.next.agentmanager.entity;

public class SnetAssetSwAuditReportTotModel {
	private String itemResult = "";
	private int cnt = 0;
	private int tot = 0;
	
	// SNET_ASSET_MASTER.AUDIT_RATE 업데이트용
	private int adWeightOk = 0;
	private int adWeightNok = 0;
	private int adWeightReq = 0;
	private int adWeightPass = 0;
	
	public String getItemResult() {
		return itemResult;
	}
	public void setItemResult(String itemResult) {
		this.itemResult = itemResult;
	}
	public int getCnt() {
		return cnt;
	}
	public void setCnt(int cnt) {
		this.cnt = cnt;
	}
	public int getTot() {
		return tot;
	}
	public void setTot(int tot) {
		this.tot = tot;
	}
	public int getAdWeightOk() {
		return adWeightOk;
	}
	public void setAdWeightOk(int adWeightOk) {
		this.adWeightOk = adWeightOk;
	}
	public int getAdWeightNok() {
		return adWeightNok;
	}
	public void setAdWeightNok(int adWeightNok) {
		this.adWeightNok = adWeightNok;
	}
	public int getAdWeightReq() {
		return adWeightReq;
	}
	public void setAdWeightReq(int adWeightReq) {
		this.adWeightReq = adWeightReq;
	}
	public int getAdWeightPass() {
		return adWeightPass;
	}
	public void setAdWeightPass(int adWeightPass) {
		this.adWeightPass = adWeightPass;
	}
}