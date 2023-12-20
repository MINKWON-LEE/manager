package com.igloosec.smartguard.next.agentmanager.exception;

import com.igloosec.smartguard.next.agentmanager.memory.NotiType;
import com.igloosec.smartguard.next.agentmanager.utils.ErrCodeUtil;

public enum SnetCommonErrCode implements ErrCodable{
    ERR_0000("ERR_0000", "Unknown Error."),
    ERR_0001("ERR_0001", "%1 - the Operation timed out."),
    ERR_0002("ERR_0002", "please check if the Agent is running or find out the port No.10226 is blocked by your firewall."),
    ERR_0003("ERR_0003", "DB Transaction Error."),
    ERR_0004("ERR_0004", "Network session has shutdown abnormally. please restart the program. (firewall...))"),
    ERR_0005("ERR_0005", "connection refused by the Switch. please check ID and PASSWORD."),
    ERR_0006("ERR_0006", "File Decryption Fail."),
    ERR_0007("ERR_0007", "KEY encryption error has occurred."),
    ERR_0008("ERR_0008", "File Not Found."),
    ERR_0009("ERR_0009", "File Stream IO ERROR."),
    ERR_0010("ERR_0010", "IO Exception"),
    ERR_0011("ERR_0011", "File Copy Error."),
    ERR_0012("ERR_0012", "Diagnosis Standards does Not exists."),
    ERR_0013("ERR_0013", "Wrong Get-Result File."),
    ERR_0014("ERR_0014", "Asset-Code is Empty."),
    ERR_0015("ERR_0015", "Error Parsing Diagnosis Results."),
    ERR_0016("ERR_0016", "Failed to Transfer Diagnosis Program. Agent in Disconnected Status. Please try it again."),
    ERR_0017("ERR_0017", "Wrong Diagnosis File Name."),
    ERR_0018("ERR_0018", "Failed to Collect Get-Result File."),
    ERR_0019("ERR_0019", "Failed to Decrypt Get-Result File or Unzip the Compressed."),
    ERR_0020("ERR_0020", "Error Parsing Get-Result File. please check it again[1]."),
    ERR_0021("ERR_0021", "Error occurred After Parsing Get-Result File.please check File Permission or Disk Capacity."),
    ERR_0022("ERR_0022", "Failed to Parse Get-Result File. please check Get-Result File[2]."),
	ERR_0023("ERR_0023", "please check the Manager Code of this asset."),
    ERR_0024("ERR_0024", "SQL Transaction Errors."),
    ERR_0025("ERR_0025", "Agent %1 : the Operation timed out."),
    ERR_0026("ERR_0026", "SQL_ERROR."),
	ERR_0027("ERR_0027", "Get-Result : could not find hostname or ip address."),
	ERR_0028("ERR_0028", "Wrong Event Diagnosis Script. please check if event diagnosis script exists or find out there is any error from the script."),
	ERR_0029("ERR_0029", "Failed tp parse the Event Diagnosis Xml File. please check the xml data."),
	ERR_0030("ERR_0030", "Failed to handle the Result Data of Event Diagnosis."),
    ERR_0031("ERR_0031", "Failed to parse Get Script Result. : there is no asset information."),
    ERR_0032("ERR_0032", "Cannot find the File for Decryption."),
    ERR_0033("ERR_0033", "Failed to Read the Online Diagnosis-Results File(XML)."),
    ERR_0034("ERR_0034", "Failed to Read the Manual Diagnosis-Results File(XML)."),
    ERR_0035("ERR_0035", "Error Reading Disposable Diagnosis Results File."),
    ERR_0036("ERR_0036", "the Hash Values of the Get-Script do Not Match."),
    ERR_0037("ERR_0037", "the Hash Values of the Diagnosis-Script do Not Match."),
    ERR_0038("ERR_0038", "the Selected Software Item is Different to the Uploaded Software Item."),
    ERR_0039("ERR_0039", "please check the execute permission for diagnosis. current user : %1, execute permitted user : %2"),
    ERR_0040("ERR_0040", "there is no DiagAgent information. (please check the information for DB diagnosis.)"),
    ERR_0041("ERR_0041", "there is no DiagAgent information"),
    ERR_0042("ERR_0042", "File Hash List ERROR");



    private String errCode;
    private String msg;

    @Override
    public String getErrCode() {
        return this.errCode;
    }
    @Override
    public String getMessage(String... args) {
        String parse = "";
        if (this.errCode == ERR_0001.getErrCode()) {
            parse = NotiType.AN061.toString() + ": " + this.msg;
        } else if (this.errCode == ERR_0006.getErrCode()) {
            parse = NotiType.AN064.toString() + ": " + this.msg;
        } else if (this.errCode == ERR_0012.getErrCode()) {
            parse = NotiType.AN065.toString() + ": " + this.msg;
        } else if (this.errCode == ERR_0013.getErrCode() || this.errCode == ERR_0020.getErrCode()) {
            parse = NotiType.AN066.toString() + ": " + this.msg;
        } else if (this.errCode == ERR_0015.getErrCode()) {
            parse = NotiType.AN067.toString() + ": " + this.msg;
        } else if (this.errCode == ERR_0019.getErrCode()) {
            parse = NotiType.AN068.toString() + ": " + this.msg;
        } else if (this.errCode == ERR_0021.getErrCode()) {
            parse = NotiType.AN069.toString() + ": " + this.msg;
        } else if (this.errCode == ERR_0023.getErrCode()) {
            parse = NotiType.AN070.toString() + ": " + this.msg;
        } else if (this.errCode == ERR_0024.getErrCode() || this.errCode == ERR_0026.getErrCode()) {
            parse = NotiType.AN071.toString() + ": " + this.msg;
        } else if (this.errCode == ERR_0027.getErrCode()) {
            parse = NotiType.AN072.toString() + ": " + this.msg;
        } else if (this.errCode == ERR_0036.getErrCode()) {
            parse = NotiType.AN057.toString() + ": " + this.msg;
        } else if (this.errCode == ERR_0037.getErrCode()) {
            parse = NotiType.AN058.toString() + ": " + this.msg;
        } else if (this.errCode == ERR_0033.getErrCode()) {
            parse = NotiType.AN062.toString() + ": " + this.msg;
        } else if (this.errCode == ERR_0034.getErrCode()) {
            parse = NotiType.AN063.toString() + ": " + this.msg;
        } else if (this.errCode == ERR_0035.getErrCode()) {
            parse = NotiType.AN073.toString() + ": " + this.msg;
        } else if (this.errCode == ERR_0038.getErrCode()) {
            parse = NotiType.AN074.toString() + ": " + this.msg;
        } else if (this.errCode == ERR_0040.getErrCode()) {
            parse = NotiType.AN075.toString() + ": " + this.msg;
        } else if (this.errCode == ERR_0041.getErrCode()) {
            parse = NotiType.AN076.toString() + ": " + this.msg;
        } else {
            parse = this.msg;
        }

        return ErrCodeUtil.parseMessage(parse, args);
    }

    SnetCommonErrCode(String errCode, String msg) {
        this.errCode = errCode;
        this.msg = msg;
    }

    SnetCommonErrCode(String errCode) {
        this.errCode = errCode;
    }
}
