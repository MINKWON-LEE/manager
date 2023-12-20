package com.igloosec.smartguard.next.agentmanager.entity;

public class SnetConfigAuditFile {
	private String auditFileCd;
	private int fileType;
	private String fileNm;
	private String swType;
	private String swNm;
	private String fileVersion;
	private String fileDesc;
	private String fileOrder;
	private String weightTot;
	private String resultTot;
	private String autoAuditYn;
	private String checksumHash;

	public String getAuditFileCd() {
		return auditFileCd;
	}

	public void setAuditFileCd(String auditFileCd) {
		this.auditFileCd = auditFileCd;
	}

	public int getFileType() {
		return fileType;
	}

	public void setFileType(int fileType) {
		this.fileType = fileType;
	}

	public String getFileNm() {
		return fileNm;
	}

	public void setFileNm(String fileNm) {
		this.fileNm = fileNm;
	}

	public String getSwType() {
		return swType;
	}

	public void setSwType(String swType) {
		this.swType = swType;
	}

	public String getSwNm() {
		return swNm;
	}

	public void setSwNm(String swNm) {
		this.swNm = swNm;
	}

	public String getFileVersion() {
		return fileVersion;
	}

	public void setFileVersion(String fileVersion) {
		this.fileVersion = fileVersion;
	}

	public String getFileDesc() {
		return fileDesc;
	}

	public void setFileDesc(String fileDesc) {
		this.fileDesc = fileDesc;
	}

	public String getFileOrder() {
		return fileOrder;
	}

	public void setFileOrder(String fileOrder) {
		this.fileOrder = fileOrder;
	}

	public String getWeightTot() {
		return weightTot;
	}

	public void setWeightTot(String weightTot) {
		this.weightTot = weightTot;
	}

	public String getResultTot() {
		return resultTot;
	}

	public void setResultTot(String resultTot) {
		this.resultTot = resultTot;
	}

	public String getAutoAuditYn() {
		return autoAuditYn;
	}

	public void setAutoAuditYn(String autoAuditYn) {
		this.autoAuditYn = autoAuditYn;
	}

	public String getChecksumHash() {
		return checksumHash;
	}

	public void setChecksumHash(String checksumHash) {
		this.checksumHash = checksumHash;
	}
}
