/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.memory.ManagerJobFactory.java
 * company : Mobigen
 * @author : Je Joong Lee
 * created at : 2016. 2. 22.
 * description : 
 */

package com.igloosec.smartguard.next.agentmanager.memory;

public interface ManagerJobFactory {
	// NOTIFICATION
	String HEALTHCHECK = "HEALTHCHECK";
	String HEALTHCHECKFIN = "HEALTHCHECKFIN";
	String RELAYHEALTHCHECK = "RELAYHEALTHCHECK";
	String AGNTUP = "AGNTUP";
	String AGNTUP2 = "AGNTUP2";
	String RELAYUP = "RELAYUP";
	String IPUPD = "IPUPD";
	String DGFILEDONE = "DGFILEDONE";
	String MANUALGSCRPTFIN = "MANUALGSCRPTFIN"; // get script 수동으로 파일 업로드
	String MULTIMANUALGSCRPTFIN = "MULTIMANUALGSCRPTFIN"; // Multi get script 수동으로 파일 업로드
	String GSCRPTFIN = "GSCRPTFIN";//get script 결과 전송
	String DGFIN ="DGFIN";
	String AGENTSETUPREQ = "AGENTSETUPREQ";
	String AGENTJARUPREQ = "AGENTJARUPREQ";
	String RUNCMD = "RCR";
	String AGENTUPDATEREQ = "AGENTUPDATEREQ";
	String GSCRPTEXECREQ = "GSCRPTEXECREQ";
	String DGEXECREQ = "DGEXECREQ";
	String EVENTDGEXECREQ = "EVENTDGEXECREQ";
	String CFGTFILE = "CFGTFILE";
	String GSCRPTFILE = "GSCRPTFILE";
	String DGFILE = "DGFILE";
	String EVENTDGFILE = "EVENTDGFILE";
	String NETWORKEXEREQ = "NETWORKEXEREQ";
	String NETWORKDGEXECREQ = "NETWORKDGEXECREQ";
	String SCHEDULENETWORKEXEREQ = "SCHEDULENETWORKEXEREQ";
	String OTP = "OTP";
	String MAIL = "MAIL"; // MAIL notification
	String SMS  = "SMS";  // SMS notification
	String MONITER  = "MONITER";  // SMS notification
	String AGENTLOG  = "AGENTLOG";  // Log Download
	String AGENTLOGFIN = "AGENTLOGFIN"; // Log Download
	String SRVMANUALGSCRPTFIN = "SRVMANUALGSCRPTFIN";  // 수동으로 네트워크 장비 config 파일을 업로드하여 .dat 파일을 만들어 장비정보 등록을 한다.
	String MULTISRVMANUALGSCRPTFIN = "MULTISRVMANUALGSCRPTFIN";  // 수동으로 네트워크 장비 config zip 파일을 업로드하여 .dat 파일을 만들어 장비정보 등록을 한다.
	String SRVMANUALDGFIN = "SRVMANUALDGFIN";  // 수동으로 네트워크 장비 config 파일을 업로드하여 .xml 파일을 만들어 진단한다.
	String SRVDGFIN = "SRVDGFIN";  // 수동으로 네트워크 장비 config 파일을 업로드하여 .dat 파일을 만들어 장비정보 등록을 한다.
	String JOBDEL = "JOBDEL";  // 수동으로 네트워크 장비 config 파일을 업로드하여 .dat 파일을 만들어 장비정보 등록을 한다.
	String ERRORFIN = "ERRORFIN";
    

	/** agent 설치전 수동 GET 스크립트 실행 **/
	String GSBFAGENT="GSBFAGENT";
	String GSBFFIN="GSBFFIN";

	/** Agent 설치 후 장비 정보 수집 **/
	String AGENTGSCRPTFILE = "AGENTGSCRPTFILE";  // Log Download
	
	String VCHKFROMAGT = "VCHKFROMAGT";  //version Check request From Agent

	String RUNQUERY = "RUNQUERY";

	/** agent 종료 명령 **/
	String KILLAGENT="KILLAGENT";

	/** agent 재시작 명령 **/
	String RESTARTAGENT="RESTARTAGENT";
	
	// DATAPARSE
	String BEGINELEMENT = "<BEGIN>";
	String ENDELEMENT = "<END>";

	//DB password file 전송 jobtype
	String DGPWFILE = "DGPWFILE";
	
	//DataParser connect_log message
	String ASSETCDMSG = "동일정보로 기 등록된 장비정보가 존재합니다.";

}
