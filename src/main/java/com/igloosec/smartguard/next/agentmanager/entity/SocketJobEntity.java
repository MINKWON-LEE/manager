/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.entity
 * company : Mobigen
 * @author : Hyeon-sik Jung
 * created at : 2016. 6. 30.
 * description : 
 */
package com.igloosec.smartguard.next.agentmanager.entity;

public class SocketJobEntity extends JobEntity {

	private int connectionRetry = 1;
	private int connectionTimeOut = 100;
	/**
	 * @return the connectionRetry
	 */
	public int getConnectionRetry() {
		return connectionRetry;
	}
	/**
	 * @param connectionRetry the connectionRetry to set
	 */
	public void setConnectionRetry(int connectionRetry) {
		this.connectionRetry = connectionRetry;
	}
	/**
	 * @return the connectionTimeOut
	 */
	public int getConnectionTimeOut() {
		return connectionTimeOut;
	}
	/**
	 * @param connectionTimeOut the connectionTimeOut to set
	 */
	public void setConnectionTimeOut(int connectionTimeOut) {
		this.connectionTimeOut = connectionTimeOut;
	}
}
