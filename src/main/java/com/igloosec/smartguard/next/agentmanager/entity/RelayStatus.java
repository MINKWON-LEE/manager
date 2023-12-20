/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.entity.RelayStatus.java
 * company : Mobigen
 * @author : Oh su jin
 * created at : 2016. 11. 19.
 * description :
 */
package com.igloosec.smartguard.next.agentmanager.entity;

public class RelayStatus {

	private String assetCd;
	
	private int relayStatus;
	
	private String relayStatusDesc;

	public String getAssetCd() {
		return assetCd;
	}

	public void setAssetCd(String assetCd) {
		this.assetCd = assetCd;
	}

	public int getRelayStatus() {
		return relayStatus;
	}

	public void setRelayStatus(int relayStatus) {
		this.relayStatus = relayStatus;
	}

	public String getRelayStatusDesc() {
		return relayStatusDesc;
	}

	public void setRelayStatusDesc(String relayStatusDesc) {
		this.relayStatusDesc = relayStatusDesc;
	}
}
