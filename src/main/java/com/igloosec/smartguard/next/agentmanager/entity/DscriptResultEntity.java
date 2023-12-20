/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.services.ResultXMLParserEntity.java
 * company : Mobigen
 *
 * @author : Jong Seong Lee
 * created at : 2016. 2. 15.
 * description :
 */

package com.igloosec.smartguard.next.agentmanager.entity;

import java.util.List;

public class DscriptResultEntity extends BaseDBEntity {
	private String auditDay;
	private SwAuditHistoryDBEntity auditHistoryDBEntity;
	private SystemInfoEntity systemInfoEntity;
	private SummaryEntity summaryEntity;
	private List<Diagnosis> diagnosis;
	private int manualAssetCount = 0;

	private ProgramEntity programEntity;

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
	 * @return the systemInfoEntity
	 */
	public SystemInfoEntity getSystemInfoEntity() {
		return systemInfoEntity;
	}

	/**
	 * @param systemInfoEntity
	 *            the systemInfoEntity to set
	 */
	public void setSystemInfoEntity(SystemInfoEntity systemInfoEntity) {
		this.systemInfoEntity = systemInfoEntity;
	}

	/**
	 * @return the summaryEntity
	 */
	public SummaryEntity getSummaryEntity() {
		return summaryEntity;
	}

	/**
	 * @param summaryEntity
	 *            the summaryEntity to set
	 */
	public void setSummaryEntity(SummaryEntity summaryEntity) {
		this.summaryEntity = summaryEntity;
	}

	/**
	 * @return the diagnosis
	 */
	public List<Diagnosis> getDiagnosis() {
		return diagnosis;
	}

	/**
	 * @param diagnosis
	 *            the diagnosis to set
	 */
	public void setDiagnosis(List<Diagnosis> diagnosis) {
		this.diagnosis = diagnosis;
	}

	/**
	 * @return the auditHistoryDBEntity
	 */
	public SwAuditHistoryDBEntity getAuditHistoryDBEntity() {
		return auditHistoryDBEntity;
	}

	/**
	 * @param auditHistoryDBEntity
	 *            the auditHistoryDBEntity to set
	 */
	public void setAuditHistoryDBEntity(
			SwAuditHistoryDBEntity auditHistoryDBEntity) {
		this.auditHistoryDBEntity = auditHistoryDBEntity;
	}

	public ProgramEntity getProgramEntity() {
		return programEntity;
	}

	public void setProgramEntity(ProgramEntity programEntity) {
		this.programEntity = programEntity;
	}


	public int getManualAssetCount() {
		return manualAssetCount;
	}

	public void setManualAssetCount(int manualAssetCount) {
		this.manualAssetCount = manualAssetCount;
	}

	@Override
	public String toString() {
		return "DscriptResultEntity [auditDay=" + auditDay
				+ ", auditHistoryDBEntity=" + auditHistoryDBEntity
				+ ", systemInfoEntity=" + systemInfoEntity + ", summaryEntity="
				+ summaryEntity + ", diagnosis=" + diagnosis + "]";
	}

}