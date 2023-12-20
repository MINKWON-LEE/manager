/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.memory.INMEMORYDB.java
 * company : Mobigen
 * @author : Je Joong Lee
 * created at : 2016. 2. 3.
 * description : 
 */

package com.igloosec.smartguard.next.agentmanager.memory;


import com.igloosec.smartguard.next.agentmanager.dao.Dao;
import com.igloosec.smartguard.next.agentmanager.entity.AgentJob;
import com.igloosec.smartguard.next.agentmanager.entity.JobEntity;
import com.igloosec.smartguard.next.agentmanager.entity.RunnigJobEntity;

import com.igloosec.smartguard.next.agentmanager.exception.SnetException;
import com.igloosec.smartguard.next.agentmanager.utils.CommonUtils;
import com.igloosec.smartguard.next.agentmanager.utils.DateUtil;
import jodd.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.*;

public class INMEMORYDB {
	Logger logger = LoggerFactory.getLogger(getClass());

	// 앞으로 배포될 버전
	public static String toBeVersion = "3.2.1.6";
	public static String smartGuardVersion = "3.1.1.0";
	public static boolean toBeVersionUse = false;
	public static boolean agentResourceUse = false;  // 에이전트 리소스 쌓을지 여부 결정
	public static int agentResourceTime = 5;  // 에이전트 이력 쌓을 시간(분단위) 설정. 디폴트 5분

    // 수집 진단 분리시
    // 수집 진단 분리된 스크립트 오류 있을 경우 대비 임시로 원래의 진단 스크립트를 보내주는 옵션
    public static boolean diagInfoNotUse = false;

	// 자동 장비 수집 설정
	public static String autoGet = "AUTOGETSG!@#";
//	public static Context context;
//	10221~10227
	public static String MONITER_PORT = "9876";
	public static String LISTENER_PORT = "10225";
	public static String AGENT_PORT = "10226";
	public static String RELAY_PORT = "10224";
	public static LinkedBlockingDeque<String> DIAGNOSISQUEUE;

	//장비정비 수집의 대량 요청시 ssh 접속이 필요한 서비스만 순서를 유지하기 위해 등록  phil
	public static LinkedBlockingDeque<String> GETQUEUE;
	public static LinkedBlockingDeque<String> AGENTSETUPQUEUE;
	public static LinkedBlockingDeque<String> NETWORKQUEUE;							// 네트워크 수집 및 진단 - 온라인

	public static ConcurrentHashMap<String, RunnigJobEntity> RUNNINGDGJOBLIST;

	//에이전트에 전달된 진단 실행 JOB
	public static ConcurrentHashMap<String, String> RUNNINGAGENTDGJOBLIST;

	//장비 정보 수집 요청이 있으면 모두 등록
	public static ConcurrentHashMap<String,RunnigJobEntity> RUNNINGGSJOBLIST;

	//네트워크 장비 정보 수집 및 네트워크 진단 실헹  요청이 있으면 모두 등록
	public static ConcurrentHashMap<String,RunnigJobEntity> RUNNINGNWJOBLIST;

	//로그수집  요청이 있으면 모두 등록
	public static ConcurrentHashMap<String,RunnigJobEntity> RUNNINGLOGJOBLIST;

	//에이전트 재시작, 중지 요청이 있으면 모두 등록
	public static ConcurrentHashMap<String, RunnigJobEntity> RUNNINGCONTROLJOBLIST;

	public static ConcurrentHashMap<String,RunnigJobEntity> RUNNINGSETUPJOBLIST;

	// 자산별로 요청 중인 모든 JOB에 있어서 에이전트를 통한 실행이 필요할 때에만 순서대로 기록 phil
	// 모든 자산 별 모든 JOB을 기록. <assetCd, jobType>
	public static ConcurrentHashMap<String, CopyOnWriteArrayList<AgentJob>> JOBLISTPERASSET;

	// 모든 ssh를 통한 실행중인 ssh 호스트 정보를 관리. phil
	// 장비별(자산별)로  ssh 기능이 하나라도 실행 중이면 실행되지 않게 막는다.
	public static ConcurrentHashMap<String, RunnigJobEntity> RUNNING_SSHJOBLIST;

	// 시간 별로 에이전트 asset_cd를 저장하여 관리.
	public static ConcurrentHashMap<Integer, CopyOnWriteArrayList<String>> AGENTSCHEDULELIST;

	// 에이전트 별로 job schedule time을 계산하여 관리.
	public static ConcurrentHashMap<String, Integer> AGENTSCHEDULEASSETLIST;

	// 에이전트 별로 job schedule api를 호출한 시간을 기록.
	public static ConcurrentHashMap<String, String> AGENTSCHEDULEASSETCHECKLIST;

	public static int maxDGreq = 0;
	public static int maxDGexec = 0;

	public static int maxGSexec = 80;
	public static int maxNWexec = 80;
	public static int maxSETUPexec = 100;

	public static String nwRunDg = "N";
	public static String dgWaitTime = "20";
	public static String agentCPUMax = "95";
	public static String agentMemoryMax = "95";
	public static int agentJobCheckCnt = 3;

	// 여러 진단요청 존재시 더 빨리 진단실행 될 수 있도록 에이전트에 조정된 시간을 전달
	public static boolean fastDelayUse = false;   // 사용 여부
	public static int fastDelayTime;           // 새로운 delayTime

	/** 장비정보 수집 프로그램 **/
	public static String GetLinux = "getunixagent";
	public static String GetWindows = "getwindows";

	/** Common Code **/
	public static String RECV = "IN";
	public static String SEND = "OUT";
	public static String OTP = "OTP";

	/** 파일 확장자 **/
	public static String OTPTYPE = ".ooo";
	public static String DESRESULTTYPE = ".des";
	public static String GSRESULTTYPE = ".dat";
	public static String DGRESULTTYPE = ".xml";
	public static String CFG = ".cfg";
	public static String JAR = ".jar";
	public static String ZIP = ".zip";
	public static String SH = ".class";
	public static String BAK = ".bak";
	public static String HASH = ".hash";
	public static String DES = ".des";
	public static String IV = ".iv";
	public static String SALT = ".salt";
	public static String PY = ".py";
	public static String LOG = ".log";

	/** CONFIG Properties **/
	public static String CONF_DIR = "/usr/local/snetManager/conf";
	public static String CONF_PROPERTIES_FILE = "AgentManager.context.yml";

	/** SHADOW DIR **/
	public static String SHADOW_DIR = "/usr/local/snetManager/txFiles/inbound/jtrResult";
	public static String SHADOW_EXTENSION = ".jtr";
	
	/** SMS **/
	public static String SKT_SMS_IP = "90.90.100.34";
	public static int    SKT_SMS_PORT = 11001;
	public static String EXE_CMD = "python /home/sgweb/service/java/SNET/util/SMSClient.py";
	/** 발신자 **/
	public static String SMS_TX_NUM = "031-710-5143";
	public static String SMS_PRJ_NAME = "SNET";
	public static String SMS_KIND = "etc";
	public static String SMS_TYPE = "etc";

	
	/** Email **/
	public static String SMTP_PORT = "25";
	public static String SMTP_HOST = "sktsmtp.sktelecom.com";
//	public static String SMTP_HOST = "150.19.1.94";
	/**	실제 발송 여부 **/
	public static Boolean SEND_MAIL_TRUE = true;
	public static String FROM_MAIL_ADDR = "snet@sk.com";
	
	/** Character **/
	public static String EUC_KR = "EUC-KR";
	public static String ISO_8859_1 = "WINDOW";

	/** OS TYPE **/
	public static String WIN_ID = "WIN";

	/** Network equitment ManagerCd **/
	public static String NW_MANAGER_CD = "T1359935510887";
	public static String NW_FTP_IP = "60.11.8.37";
	public static int NW_FTP_PORT = 21;
	public static String NW_FTP_ID = "sdncfgftp";
	public static String NW_FTP_PW = "!sdncfgftp";

	public static String latestAgentVersion = "1.2.2";

	/*** Agent Result ***/
	public static String localAESpath = "./download/";
	
	/***System Type Variables***/
	public static String MANAGER_OS_TYPE = "LINUX"; //="LINUX" //="WINDOWS"
	public static String PATH_SLASH_WIN = "\\";
	public static String PATH_SLASH_UNIX = "/";
	public static String MANAGER_USE_SLASH = "";
	public static String DELIMITER = "[^]";
	

	/***Agent SET-UP***/
	public static String AGENT_SYS_ROOT_DIR_WIN = "C:[SLASH]snet[SLASH]";
	public static String AGENT_SYS_ROOT_DIR_UNIX = "[SLASH]usr[SLASH]local[SLASH]snet[SLASH]";
	public static String MANAGER_SYS_ROOT_DIR_WIN ="C:[SLASH]usr[SLASH]local[SLASH]snetManager[SLASH]";
	public static String MANAGER_SYS_ROOT_DIR ="[SLASH]usr[SLASH]local[SLASH]snetManager[SLASH]";
	public static String AGENT_SETUP_SCRIPT_FILENAME = "agentsetup";

	/***Manager에서 생성되어 있는 스크립트 파일 경로***/
	public static String GETSCRIPT_WINDOWS_MANAGER_DIR = "[MANAGER_SYS_ROOT_DIR]txFiles[SLASH]outbound[SLASH]get[SLASH]windows[SLASH]";
	public static String GETSCRIPT_UNIX_MANAGER_DIR = "[MANAGER_SYS_ROOT_DIR]txFiles[SLASH]outbound[SLASH]get[SLASH]unix[SLASH]";
	public static String DIAG_WINDOWS_MANAGER_DIR = "[MANAGER_SYS_ROOT_DIR]txFiles[SLASH]outbound[SLASH]diags[SLASH]windows[SLASH]";
	public static String DIAG_UNIX_MANAGER_DIR = "[MANAGER_SYS_ROOT_DIR]txFiles[SLASH]outbound[SLASH]diags[SLASH]unix[SLASH]";

	// 수집 진단 분리시
	// 수집 진단 분리된 스크립트 오류 있을 경우 대비 임시로 원래의 진단 스크립트를 보내주는 옵션
	// 우선 리눅스만 적용
	public static String DIAG_UNIX_MANAGER_DINFONOTUSE_DIR = "[MANAGER_SYS_ROOT_DIR]txFiles[SLASH]outbound[SLASH]diags[SLASH]unix_dinfonotuse[SLASH]";

	/***Manager에서 전송할 파일들의 경로***/
	public static String SETUP_FILE_MANAGER_DIR = "[MANAGER_SYS_ROOT_DIR]txFiles[SLASH]outbound[SLASH]setups[SLASH]install[SLASH]online[SLASH]";
	public static String SETUP_FILE_MANAGER_JOON_DIR = "[MANAGER_SYS_ROOT_DIR]txFiles[SLASH]outbound[SLASH]setups[SLASH]install[SLASH]online_j[SLASH]";
	public static String GETSCRIPT_FILE_MANAGER_DIR = "[MANAGER_SYS_ROOT_DIR]txFiles[SLASH]outbound[SLASH]get[SLASH]work[SLASH]";
	public static String DIAG_FILE_MANAGER_DIR = "[MANAGER_SYS_ROOT_DIR]txFiles[SLASH]outbound[SLASH]diags[SLASH]work[SLASH]";
	public static String OTP_FILE_MANAGER_DIR = "[MANAGER_SYS_ROOT_DIR]txFiles[SLASH]outbound[SLASH]otp[SLASH]";
	 
	/***Manager내 받은 실행 결과 파일들의 경로***/
	public static String DIAG_RESULT_FILE_MANAGER_DIR = "[MANAGER_SYS_ROOT_DIR]txFiles[SLASH]inbound[SLASH]diagResults[SLASH]";
	public static String NWCFG_RESULT_FILE_MANAGER_DIR = "[MANAGER_SYS_ROOT_DIR]txFiles[SLASH]inbound[SLASH]nwResults[SLASH]";
	public static String NWCFG_RESULT_FTP_FILE_MANAGER_DIR = "[MANAGER_SYS_ROOT_DIR]txFiles[SLASH]inbound[SLASH]nwResults[SLASH]ftp[SLASH]";
	public static String GETSCRIPT_RESULT_FILE_MANAGER_DIR = "[MANAGER_SYS_ROOT_DIR]txFiles[SLASH]inbound[SLASH]getResults[SLASH]";
	public static String OTP_RESULT_FILE_MANAGER_DIR = "[MANAGER_SYS_ROOT_DIR]txFiles[SLASH]inbound[SLASH]otpResults[SLASH]";
	public static String LOG_FILE_MANAGER_DIR = "[MANAGER_SYS_ROOT_DIR]txFiles[SLASH]inbound[SLASH]logdir[SLASH]";

	/***Manager내 수집진단 분리시 진단 스크립트 실행 경로***/
	public static String DIAG_INFO_FILE_MANAGER_DIR = "[MANAGER_SYS_ROOT_DIR]txFiles[SLASH]diagDir[SLASH]";

	/***Tomcat에서 수집된 환경파일을 다운로드 하는 경로***/
	public static String DIAG_INFO_FILE_TOMCAT_DIR = "[MANAGER_SYS_ROOT_DIR]txFiles[SLASH]tomcat[SLASH]diagInfo[SLASH]";

	/** Shadow 파일 저장 경로 **/
	public static String SHADOW_MANAGER_DIR = "[MANAGER_SYS_ROOT_DIR]txFiles[SLASH]inbound[SLASH]jtrResult[SLASH]";
	public static String JTR_SHELL = "john.sh";
	
	public static String KNOWN_HOSTS="[MANAGER_SYS_ROOT_DIR]knownhosts.host";
	public static String MODULES_VERSION="[MANAGER_SYS_ROOT_DIR]modules_version";

	/***Manager내 받은 진단결과 파일 백업 경로***/
	public static String DIAG_RESULT_FILE_BACKUP = "[MANAGER_SYS_ROOT_DIR]txFiles[SLASH]backup[SLASH]";
	
	/***Agent내  실행 결과 파일들의 경로***/
	public static String DIAG_RESULT_FILE_AGENT_DIR = "[AGENT_SYS_ROOT_DIR]txFiles[SLASH]outbound[SLASH]diag[SLASH]";
	public static String GETSCRIPT_RESULT_FILE_AGENT_DIR = "[AGENT_SYS_ROOT_DIR]txFiles[SLASH]outbound[SLASH]get[SLASH]";
	
	/***Agent내 받은  파일들의 경로***/
	public static String DIAG_RECV_FILE_AGENT_ROOT_DIR = "[AGENT_SYS_ROOT_DIR]txFiles[SLASH]inbound[SLASH]diag[SLASH]";
	public static String GETSCRIPT_RECV_FILE_AGENT_ROOT_DIR = "[AGENT_SYS_ROOT_DIR]txFiles[SLASH]inbound[SLASH]get[SLASH]";
	public static String OTP_RECV_FILE_AGENT_ROOT_DIR = "[AGENT_SYS_ROOT_DIR]txFiles[SLASH]inbound[SLASH]otp[SLASH]";
	public static String SETUP_RECV_FILE_AGENT_DIR = "snetsetup";
	public static String GETSCRIPT_RECV_FILE_BF_AGENT_DIR = "bfags";
	
	/***Agent내 보낼OTP 파일 경로***/
	public static String OTP_SEND_FILE_AGENT_ROOT_DIR = "[AGENT_SYS_ROOT_DIR]txFiles[SLASH]outbound[SLASH]otp[SLASH]";
	
	/***Agent내 보낼OTP 파일 경로***/
	public static String AGENT_ROOT_DIR = "[AGENT_SYS_ROOT_DIR]agent[SLASH]";
	public static String AGENT_ROOT_TRASH_DIR = "[AGENT_SYS_ROOT_DIR]agent_trash[SLASH]";
	public static String AGENT_LIBS_DIR = "[AGENT_SYS_ROOT_DIR]agent[SLASH]libs[SLASH]";
	public static String AGENT_BIN_DIR = "[AGENT_SYS_ROOT_DIR]agent[SLASH]bin[SLASH]";
	public static String AGENT_JRE_DIR = "[AGENT_SYS_ROOT_DIR]agent[SLASH]jre[SLASH]";

	/** 수동으로 업로드된 Get, 진단 파일 경로**/
	public static String MANUAL_GSR = "[SLASH]home[SLASH]sgwas[SLASH]diagnosis[SLASH]";
	public static String MANUAL_DIAGR = "[SLASH]home[SLASH]sgwas[SLASH]diagnosis[SLASH]";
	
	/** 사용자 등록 이벤트 진단 스크립트 경로 **/
	public static String EVENT_DIAG_SCRIPT = "[SLASH]home[SLASH]sgwas[SLASH]service[SLASH]systemInfo[SLASH]user_program[SLASH]";

	/** 원격지 Agent 기동 명령어 **/
	public static String RUN_AGENT_SCRIPT = "[AGENT_SYS_ROOT_DIR]agent[SLASH]bin[SLASH]run.sh \n";
	public static String RUN_AGENT2_SCRIPT = "[AGENT_SYS_ROOT_DIR]agent[SLASH]bin[SLASH]run2.sh \n";
	public static String RUN_RELAY_SCRIPT = "[AGENT_SYS_ROOT_DIR]agent[SLASH]bin[SLASH]run_relay.sh \n";

	/** 원격지 RelayAgent 기동 명령어 **/
	public static String RUN_RELAY_AGENT_SCRIPT = "[AGENT_SYS_ROOT_DIR]agent[SLASH]bin[SLASH]run_relay.sh \n";
	
	/** 원격지 RelayAgent 중지 명령어 **/
	public static String KILL_RELAY_AGENT_SCRIPT = "[AGENT_SYS_ROOT_DIR]agent[SLASH]bin[SLASH]kill_relay.sh \n";

    /** 원격지 Agent 중지 명령어 **/
    public static String KILL_AGENT_SCRIPT = "[AGENT_SYS_ROOT_DIR]agent[SLASH]bin[SLASH]kill.sh \n";
	
	/** unix common diagnosis files **/
	public static String diagGetUnixAgent = "getunixagent.class";
	public static String diagEventUnixAgent = "eventunixagent.class";
	public static String diagInfoAgent = "diaginfo.class";
	public static String CHANGE_ENCODING = "changeEncoding.class";



	/*** OTP 인증 타임(second) **/
	public static int ValidationOTPTime = 180;
	public static int GScrpOTPTime = 180;
	public static int JobDGTimeOut = 1000*60*30;
	public static int JobGSTimeOut = 1000*60*30;
	public static int JobNWTimeOut = 1000*60*30;
	public static int JobSetupTimeOut = 1000*60*10;
	public static int JobLogTimeOut = 1000*60*30;
	public static int JobControlTimeOut = 1000*60*30;
	public static int DefaultJobSchedule = 1000*60*5;   // #에이전트 하나당 5분마다 호출.  default : 5분 (300000 mils)
	public static int EmptyJobSchedule = 1000*60*60;     // #에이전트코드가 서버에 없는 장비가 JobApi 호출. default : 1시간
	public static int JobApiTimeOut = 1000*60*30;
	public static boolean MultiServices = false;
	public static boolean DiagExcept = false;
	public static boolean useNotification = false;
	public static boolean useSSL = true;

	/* Log4J 취약점 점검 옵션 */
 	// Log4J 취약점 현황 수집 여부
	public static boolean useLog4JChecker = false;
	// Log4J 취약점 현황 자산당 수집 개수
	public static int useLog4JgetCnt = 0;
	// Log4J 취약점 실행 중인 프로세스로 찾기(false), Log4J 파일로 찾기(true)
	public static boolean useLog4JFile = false;
	// Log4J 취약점 파일로 찾을 경우 (useLog4JFile가 true일떄)
	// 찾아볼 최상위 경로 ( ";"로 여러개 입력 가능 )
	public static String useLog4JPath = "";

	// useLog4JFile와 useLog4JPath 정보를 파일로 생성.
	public static String useLog4JConfig = "log4j.conf";

	// windows 용 Log4J 취약점 점검
	public static String Log4JScan = "log4j2-scan.exe";
	public static String Log4JChker = "log4jchecker.bat";
	public static String Lgg4JParse = "w_parse.vbs";
	public static String Log4Jfind = "w_find_files.vbs";
	public static String Log4JChkZIP = "log4jchecker.zip";

	// unix 용 Log4J 취약점 점검
	public static String Log4JScanLinux = "log4j2-scan";
	public static String Log4JScanUnix = "log4j2-scan.jar";
	public static String Log4JChkerUnix = "log4jchecker.class";
	public static String Log4JChkZIPUnix = "log4junixchecker.class";

	public static int readTimeout = 10*1000; // wait 59 sec
	public static int readPtyTimeout = 5*1000; // wait 5 sec
	public static int timeOutMulMax = 24;

	public static int GetOptUse = 0;

	public static boolean useDiagOption = false;

	public static String GetDisableFunc = "";

	public static String DgScriptDiasbleFile = "dgScript.disable";

	public static String DgScriptDisableExt = ".disable";
	public static String KEYSTOREPATH ="[MANAGER_SYS_ROOT_DIR]manager[SLASH]libs[SLASH]secure[SLASH]snetsecure";

	public enum ptyKey {PTY,STR,INT,ARRSTR}

	public enum ptyMode {CMD,LOGIN,VERIFYPROMPT}
	
//	public static String RSAFilePath = "C:\\keytest\\secgw\\id_rsa_2048_relaly";
	public static String RSAFilePath = "/usr/local/snetManager/manager/libs/secure/id_rsa";
//	public static String RSAFilePath = "/home/sgweb/.ssh/id_rsa";
	public static String RSAFilePhrase = "SnetSmart!202001";
//	public static String RSAFilePhrase = "secgwpw1!";
	public static String RSAaccount = "smartuser"; //Telecom USE ONLY. 프로퍼티 노출을 꺼려 소스에 하드코딩함.
	
//	private ThreadPoolTaskExecutor taskExecutor;
	
	public static String ptyPromptPhraseVals = "$[SPACE],$,#[SPACE],#,>[SPACE],>,%[SPACE],%,]#[SPACE],]#,]$[SPACE],]$[SPACE],],][SPACE]";
	public static String ptyIntPromptPhraseVals = "36[SPACE]32,36[SPACE]32[SPACE],36,35[SPACE]32,35,62[SPACE]32,62,37[SPACE]32,37,93[SPACE]35[SPACE]32,93[SPACE]35,93[SPACE]36[SPACE]32,93[SPACE]36,93,93[SPACE]32";
	public static String ptyPassPhraseVals = "Password:[SPACE],Password:,PassWord:[SPACE],PassWord:,password[SPACE]for,Password[SPACE]for";
	public static String ptyIntPassPraseVals = "207[SPACE]200[SPACE]163[SPACE]58,148[SPACE]237[SPACE]152[SPACE]184[SPACE]58,115[SPACE]119[SPACE]111[SPACE]114[SPACE]100[SPACE]58[SPACE]32";
	public static String ptyLoginPhraseVals = "login:[SPACE],login:";
	public static String ptyLoginExcludeVals = "Last[SPACE]login:,incorrect,fail,Last[SPACE]successful[SPACE]login:";
	public static String ptySslConfirmPhraseVals = "CONTINUE";
	public static String ptyIncorrectVals = "incorrect,refuse,can[SPACE]not,sorry,unknown[SPACE]character,authentication[SPACE]failure";
	public static String ptyIntIncorrectVals = "237[SPACE]151[SPACE]136[SPACE]234[SPACE]176[SPACE]128[SPACE]32[SPACE]234[SPACE]177[SPACE]176[SPACE]235[SPACE]182[SPACE]128";
	public static String ptyIncorrectExcludeVals = "Last[SPACE]authentication[SPACE]failure";

	/*
	* 237 151 136 234 176 128 32 234 177 176 235 182 128 : 허가 거부
	* 
	*/
	
	public static String[] ptyIntPassPrase;
	public static String[] ptyPassPhrase;
	public static String[] ptyPromptPhrase;
	public static String[] ptyIntPromptPhrase;
	public static String[] ptyLoginPhrase;
	public static String[] ptyLoginExclude;
	public static String[] ptySslConfirmPhrase;//CONTINUE CONNECTION (YES/NO)?  continue connecting (yes/no)?
	public static String[] ptyIncorrect;
	public static String[] ptyIntIncorrect;
	public static String[] ptyIncorrectExclude;
	
	public static ExecutorService executorThreadPool = Executors.newFixedThreadPool(20);

	public static String serverStartedAt;

	/* 서버에서 진단 실행시 진단실행 대기 시간 */
	public static String EXEC_WAIT_TIME = "5"; //분 단위.
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void init(){
		RUNNINGDGJOBLIST = new ConcurrentHashMap<String, RunnigJobEntity>();
		RUNNINGAGENTDGJOBLIST = new ConcurrentHashMap<String, String>();
		RUNNINGGSJOBLIST = new ConcurrentHashMap<String, RunnigJobEntity>();
		RUNNINGNWJOBLIST = new ConcurrentHashMap<String, RunnigJobEntity>();
		RUNNINGSETUPJOBLIST = new ConcurrentHashMap<String, RunnigJobEntity>();
		RUNNINGLOGJOBLIST = new ConcurrentHashMap<String, RunnigJobEntity>();
		RUNNINGCONTROLJOBLIST = new ConcurrentHashMap<String, RunnigJobEntity>();

		DIAGNOSISQUEUE = new LinkedBlockingDeque();
		GETQUEUE = new LinkedBlockingDeque();
		AGENTSETUPQUEUE = new LinkedBlockingDeque();
		NETWORKQUEUE = new LinkedBlockingDeque();

		JOBLISTPERASSET = new ConcurrentHashMap<String, CopyOnWriteArrayList<AgentJob>>();
		RUNNING_SSHJOBLIST = new ConcurrentHashMap<String, RunnigJobEntity>();

		AGENTSCHEDULELIST = new ConcurrentHashMap<Integer, CopyOnWriteArrayList<String>>();
		AGENTSCHEDULEASSETLIST = new ConcurrentHashMap<String, Integer>();
		AGENTSCHEDULEASSETCHECKLIST = new ConcurrentHashMap<String, String>();
	}
	
	public void initPtyPhrases(){
		ptyPromptPhraseVals =  StringUtil.replace(ptyPromptPhraseVals, "[SPACE]", " ");
		ptyIntPromptPhraseVals =  StringUtil.replace(ptyIntPromptPhraseVals, "[SPACE]", " ");
		ptyPassPhraseVals =  StringUtil.replace(ptyPassPhraseVals, "[SPACE]", " ");
		ptyIntPassPraseVals =  StringUtil.replace(ptyIntPassPraseVals, "[SPACE]", " ");
		ptyLoginPhraseVals = StringUtil.replace(ptyLoginPhraseVals, "[SPACE]", " ");
		ptyLoginExcludeVals =   StringUtil.replace(ptyLoginExcludeVals, "[SPACE]", " ");
		ptySslConfirmPhraseVals =  StringUtil.replace(ptySslConfirmPhraseVals, "[SPACE]", " ");
		ptyIncorrectVals =  StringUtil.replace(ptyIncorrectVals, "[SPACE]", " ");
		ptyIntIncorrectVals = StringUtil.replace(ptyIntIncorrectVals, "[SPACE]", " ");
		ptyIncorrectExcludeVals = StringUtil.replace(ptyIncorrectExcludeVals, "[SPACE]", " ");
		
		
		logger.debug("ptyPromptPhraseVals:" + ptyPromptPhraseVals);
		
		ptyPromptPhrase     	=  		ptyPromptPhraseVals.split(",");
		ptyIntPromptPhrase      = 		ptyIntPromptPhraseVals.split(",");
		ptyPassPhrase           =		ptyPassPhraseVals.split(",");
		ptyIntPassPrase         =		ptyIntPassPraseVals.split(",");
		ptyLoginPhrase          =		ptyLoginPhraseVals.split(",");
		ptyLoginExclude         =		ptyLoginExcludeVals.split(",");
		ptySslConfirmPhrase     =		ptySslConfirmPhraseVals.split(",");
		ptyIncorrect            =		ptyIncorrectVals.split(",");	
		ptyIntIncorrect			= 		ptyIntIncorrectVals.split(",");
		ptyIncorrectExclude     = 		ptyIncorrectExcludeVals.split(",");
		
		
		logger.debug("--------------------------PHRASE CHECK 1----------------------");
		for(String ph : ptyPromptPhrase){
			logger.debug("ptyPromptPhrase CHECK :"+ph);	
		}
		logger.debug("--------------------------PHRASE CHECK 2----------------------");
		for(String ph : ptyIntPromptPhrase){
			logger.debug("ptyIntPromptPhrase CHECK :"+ph);	
		}
		logger.debug("--------------------------PHRASE CHECK 3----------------------");
		for(String ph : ptyIncorrect){
			logger.debug("ptyIncorrect CHECK :"+ph);	
		}
		logger.debug("--------------------------PHRASE CHECK 4----------------------");
		for(String ph : ptyIntIncorrect){
			logger.debug("ptyIntIncorrect CHECK :"+ph);	
		}
		
		logger.debug("--------------------------PHRASE CHECK 5----------------------");
		
	}

	public static String getAgentSlashType(String osType) {
		if(osType != null && osType.toUpperCase().contains("WIN")){
			return PATH_SLASH_WIN;
		}
		else{
			return PATH_SLASH_UNIX;
		}
	}
	
	private static String replaceDir(String org, String os) {
		String msysroot = "";
		if (os.equals("WIN")){			
			msysroot = MANAGER_SYS_ROOT_DIR_WIN;	
		}else{			
			msysroot = MANAGER_SYS_ROOT_DIR;
		}
		
		String result = StringUtil.replace(org,"[MANAGER_SYS_ROOT_DIR]",msysroot);
		result = StringUtil.replace(result, "[SLASH]",getAgentSlashType(os));
		
		return result;
	} 
	
	public void initManagerDirs() {
		String osType = System.getProperty("os.name").toLowerCase();
		if (osType.contains("win")) {
			MANAGER_OS_TYPE = "WINDOWS";
		} else {
			MANAGER_OS_TYPE = "LINUX";
		}

		if (MANAGER_OS_TYPE.contains("WIN")) {
			MANAGER_SYS_ROOT_DIR = MANAGER_SYS_ROOT_DIR_WIN;
			SETUP_FILE_MANAGER_DIR = replaceDir(SETUP_FILE_MANAGER_DIR,"WIN");
			SETUP_FILE_MANAGER_JOON_DIR = replaceDir(SETUP_FILE_MANAGER_JOON_DIR,"WIN");
			GETSCRIPT_FILE_MANAGER_DIR = replaceDir(GETSCRIPT_FILE_MANAGER_DIR,"WIN");
			DIAG_FILE_MANAGER_DIR = replaceDir(DIAG_FILE_MANAGER_DIR,"WIN");
			OTP_FILE_MANAGER_DIR =replaceDir(OTP_FILE_MANAGER_DIR,"WIN");
			DIAG_RESULT_FILE_MANAGER_DIR = replaceDir(DIAG_RESULT_FILE_MANAGER_DIR,"WIN");
			GETSCRIPT_RESULT_FILE_MANAGER_DIR = replaceDir(GETSCRIPT_RESULT_FILE_MANAGER_DIR,"WIN");
			OTP_RESULT_FILE_MANAGER_DIR = replaceDir(OTP_RESULT_FILE_MANAGER_DIR,"WIN");
			KNOWN_HOSTS = replaceDir(KNOWN_HOSTS,"WIN");
			MODULES_VERSION = replaceDir(MODULES_VERSION,"WIN");
			KEYSTOREPATH = replaceDir(KEYSTOREPATH,"WIN");
			DIAG_UNIX_MANAGER_DIR = replaceDir(DIAG_UNIX_MANAGER_DIR,"WIN");
			GETSCRIPT_UNIX_MANAGER_DIR = replaceDir(GETSCRIPT_UNIX_MANAGER_DIR,"WIN");
			NWCFG_RESULT_FILE_MANAGER_DIR = replaceDir(NWCFG_RESULT_FILE_MANAGER_DIR,"WIN");
			GETSCRIPT_WINDOWS_MANAGER_DIR = replaceDir(GETSCRIPT_WINDOWS_MANAGER_DIR,"WIN");
			DIAG_WINDOWS_MANAGER_DIR = replaceDir(DIAG_WINDOWS_MANAGER_DIR,"WIN");
			NWCFG_RESULT_FTP_FILE_MANAGER_DIR = replaceDir(NWCFG_RESULT_FTP_FILE_MANAGER_DIR,"WIN");
			LOG_FILE_MANAGER_DIR			=	replaceDir(LOG_FILE_MANAGER_DIR,"WIN");
			DIAG_INFO_FILE_MANAGER_DIR  =   replaceDir(DIAG_INFO_FILE_MANAGER_DIR, "WIN");
			DIAG_INFO_FILE_TOMCAT_DIR  =   replaceDir(DIAG_INFO_FILE_TOMCAT_DIR, "WIN");
			SHADOW_MANAGER_DIR			=	replaceDir(SHADOW_MANAGER_DIR,"WIN");
			MANUAL_GSR                  =   "C:" + replaceDir(MANUAL_GSR, "WIN");
			MANUAL_DIAGR				=   "C:" + replaceDir(MANUAL_DIAGR, "WIN");
			EVENT_DIAG_SCRIPT				=   "C:" + replaceDir(EVENT_DIAG_SCRIPT, "WIN");
			DIAG_UNIX_MANAGER_DINFONOTUSE_DIR = replaceDir(DIAG_UNIX_MANAGER_DINFONOTUSE_DIR,"WIN");


			MANAGER_USE_SLASH = PATH_SLASH_WIN;
		} else {
			SETUP_FILE_MANAGER_DIR = replaceDir(SETUP_FILE_MANAGER_DIR,"UNIX");
			SETUP_FILE_MANAGER_JOON_DIR = replaceDir(SETUP_FILE_MANAGER_JOON_DIR,"UNIX");
			GETSCRIPT_FILE_MANAGER_DIR = replaceDir(GETSCRIPT_FILE_MANAGER_DIR,"UNIX");
			DIAG_FILE_MANAGER_DIR = replaceDir(DIAG_FILE_MANAGER_DIR,"UNIX");
			OTP_FILE_MANAGER_DIR =replaceDir(OTP_FILE_MANAGER_DIR,"UNIX");
			DIAG_RESULT_FILE_MANAGER_DIR = replaceDir(DIAG_RESULT_FILE_MANAGER_DIR,"UNIX");
			GETSCRIPT_RESULT_FILE_MANAGER_DIR = replaceDir(GETSCRIPT_RESULT_FILE_MANAGER_DIR,"UNIX");
			OTP_RESULT_FILE_MANAGER_DIR = replaceDir(OTP_RESULT_FILE_MANAGER_DIR,"UNIX");
			KNOWN_HOSTS = replaceDir(KNOWN_HOSTS,"UNIX");
			MODULES_VERSION = replaceDir(MODULES_VERSION,"UNIX");
			KEYSTOREPATH = replaceDir(KEYSTOREPATH,"UNIX");
			DIAG_UNIX_MANAGER_DIR = replaceDir(DIAG_UNIX_MANAGER_DIR,"UNIX");
			GETSCRIPT_UNIX_MANAGER_DIR = replaceDir(GETSCRIPT_UNIX_MANAGER_DIR,"UNIX");
			NWCFG_RESULT_FILE_MANAGER_DIR = replaceDir(NWCFG_RESULT_FILE_MANAGER_DIR,"UNIX");
			GETSCRIPT_WINDOWS_MANAGER_DIR = replaceDir(GETSCRIPT_WINDOWS_MANAGER_DIR,"UNIX");
			DIAG_WINDOWS_MANAGER_DIR = replaceDir(DIAG_WINDOWS_MANAGER_DIR,"UNIX");
			NWCFG_RESULT_FTP_FILE_MANAGER_DIR = replaceDir(NWCFG_RESULT_FTP_FILE_MANAGER_DIR,"UNIX");
			LOG_FILE_MANAGER_DIR			=	replaceDir(LOG_FILE_MANAGER_DIR,"UNIX");
			SHADOW_MANAGER_DIR			=	replaceDir(SHADOW_MANAGER_DIR,"UNIX");
			DIAG_INFO_FILE_MANAGER_DIR  =   replaceDir(DIAG_INFO_FILE_MANAGER_DIR, "UNIX");
			DIAG_INFO_FILE_TOMCAT_DIR  =   replaceDir(DIAG_INFO_FILE_TOMCAT_DIR, "UNIX");
			MANUAL_GSR                  =   replaceDir(MANUAL_GSR,"UNIX");
			MANUAL_DIAGR                =   replaceDir(MANUAL_DIAGR,"UNIX");
			EVENT_DIAG_SCRIPT           =   replaceDir(EVENT_DIAG_SCRIPT,"UNIX");
			DIAG_UNIX_MANAGER_DINFONOTUSE_DIR = replaceDir(DIAG_UNIX_MANAGER_DINFONOTUSE_DIR,"UNIX");

			MANAGER_USE_SLASH = PATH_SLASH_UNIX;
		}
	}
	
	/**
	 * absolutePath
	 * @param 
	 * pathName: [SETUP_FILE_DIR, GETSCRIPT_FILE_DIR, DIAG_FILE_DIR, DIAG_RESULT_FILE_MANAGER_DIR, GETSCRIPT_RESULT_FILE_MANAGER_DIR, DIAG_RECV_FILE_AGENT_ROOT_DIR, GETSCRIPT_RECV_FILE_AGENT_ROOT_DIR, OTP_RECV_FILE_AGENT_ROOT_DIR ...]
	 * osType:[WIN, UNIX]
	 * 
	 * usage i.e. String absolutePath = INMEMORYDB.absolutePath(INMEMORYDB.SETUP_FILE_DIR,"UNIX");
	 * **/
	public static String absolutePath(String pathName , String osType) {
		String absolutePath = pathName;
		
		if (osType.contains("WIN")) {
			if(absolutePath.contains("AGENT")) {
				absolutePath =  StringUtil.replace(absolutePath, "[AGENT_SYS_ROOT_DIR]", AGENT_SYS_ROOT_DIR_WIN); 
			}			
			absolutePath = StringUtil.replace(absolutePath, "[SLASH]", PATH_SLASH_WIN);
		} else {
			if (absolutePath.contains("AGENT")) {
				absolutePath =  StringUtil.replace(absolutePath, "[AGENT_SYS_ROOT_DIR]", AGENT_SYS_ROOT_DIR_UNIX); 
			}else{
				absolutePath =  StringUtil.replace(absolutePath, "[MANAGER_SYS_ROOT_DIR]", MANAGER_SYS_ROOT_DIR);
			}
			absolutePath = StringUtil.replace(absolutePath, "[SLASH]", PATH_SLASH_UNIX);
		}
		
		
		return absolutePath;
	}

	public static String jobTypeAbsolutePath(String pathType , String jobType) {
		String absolutePath;
		String os = System.getProperty("os.name");

		if (pathType.equalsIgnoreCase(RECV)) {
			if (jobType.equalsIgnoreCase(ManagerJobFactory.OTP)) {
				absolutePath = StringUtil.replace(OTP_RESULT_FILE_MANAGER_DIR, "[MANAGER_SYS_ROOT_DIR]", MANAGER_SYS_ROOT_DIR);
			} else if (jobType.equalsIgnoreCase(ManagerJobType.AJ200.toString()) || jobType.equalsIgnoreCase(ManagerJobType.AJ201.toString())
					|| jobType.equalsIgnoreCase(ManagerJobType.WM100.toString()) || jobType.equalsIgnoreCase(ManagerJobType.WM200.toString())
					|| jobType.equalsIgnoreCase(ManagerJobType.WM301.toString())) {

				absolutePath = StringUtil.replace(GETSCRIPT_RESULT_FILE_MANAGER_DIR, "[MANAGER_SYS_ROOT_DIR]", MANAGER_SYS_ROOT_DIR);
			} else if(jobType.equalsIgnoreCase(ManagerJobType.AJ100.toString())	|| jobType.equalsIgnoreCase(ManagerJobType.AJ101.toString())
					|| jobType.equalsIgnoreCase(ManagerJobType.WM102.toString())|| jobType.equalsIgnoreCase(ManagerJobType.WM202.toString())
					|| jobType.equalsIgnoreCase(ManagerJobType.WM303.toString())) {

				absolutePath = StringUtil.replace(DIAG_RESULT_FILE_MANAGER_DIR, "[MANAGER_SYS_ROOT_DIR]", MANAGER_SYS_ROOT_DIR);
			}else if(jobType.equalsIgnoreCase(ManagerJobType.WM300.toString())) {

				absolutePath = StringUtil.replace(NWCFG_RESULT_FILE_MANAGER_DIR, "[MANAGER_SYS_ROOT_DIR]", MANAGER_SYS_ROOT_DIR);
			} else if(jobType.equalsIgnoreCase(ManagerJobType.AJ300.toString())){

				absolutePath = StringUtil.replace(LOG_FILE_MANAGER_DIR, "[MANAGER_SYS_ROOT_DIR]", MANAGER_SYS_ROOT_DIR);
			} else {
				absolutePath = "";
			}
		} else if(pathType.equalsIgnoreCase(SEND)) {
			if (jobType.equalsIgnoreCase(ManagerJobFactory.OTP)) {
				absolutePath =  StringUtil.replace(OTP_FILE_MANAGER_DIR, "[MANAGER_SYS_ROOT_DIR]", MANAGER_SYS_ROOT_DIR);
			} else if (jobType.equalsIgnoreCase(ManagerJobType.AJ200.toString())){
				absolutePath =  StringUtil.replace(GETSCRIPT_FILE_MANAGER_DIR, "[MANAGER_SYS_ROOT_DIR]", MANAGER_SYS_ROOT_DIR);
			} else if (jobType.equalsIgnoreCase(ManagerJobType.AJ100.toString())
					|| jobType.equalsIgnoreCase(ManagerJobType.AJ101.toString())
					|| jobType.equalsIgnoreCase(ManagerJobFactory.DGPWFILE)) {
				absolutePath =  StringUtil.replace(DIAG_FILE_MANAGER_DIR, "[MANAGER_SYS_ROOT_DIR]", MANAGER_SYS_ROOT_DIR);
			} else {
				absolutePath = "";
			}
		} else {
			absolutePath = "";
		}

		if (os != null && os.toUpperCase().contains(WIN_ID)) {
			absolutePath = StringUtil.replace(absolutePath, "[SLASH]", PATH_SLASH_WIN);
		}
		else {
			absolutePath = StringUtil.replace(absolutePath, "[SLASH]", PATH_SLASH_UNIX);
		}


		return absolutePath;
	}

	public static String jobTypeAbsolutePath(String pathDir){
		String absolutePath;

		absolutePath = StringUtil.replace(pathDir, "[MANAGER_SYS_ROOT_DIR]", MANAGER_SYS_ROOT_DIR);
		absolutePath = StringUtil.replace(absolutePath, "[SLASH]", File.separator);

		return absolutePath;
	}

	public static String scriptFileAbsolutePath(String jobType, String osType) {
		String absolutePath;
		String os = System.getProperty("os.name");

		if (jobType.equalsIgnoreCase(ManagerJobType.AJ200.toString())) {
			if (osType.toUpperCase().contains(INMEMORYDB.WIN_ID)){
				absolutePath =  StringUtil.replace(GETSCRIPT_WINDOWS_MANAGER_DIR, "[MANAGER_SYS_ROOT_DIR]", MANAGER_SYS_ROOT_DIR);
			}else{
				absolutePath =  StringUtil.replace(GETSCRIPT_UNIX_MANAGER_DIR, "[MANAGER_SYS_ROOT_DIR]", MANAGER_SYS_ROOT_DIR);
			}

		} else if (jobType.equalsIgnoreCase(ManagerJobType.AJ100.toString())) {
			if (osType.toUpperCase().contains(INMEMORYDB.WIN_ID)) {
				absolutePath =  StringUtil.replace(DIAG_WINDOWS_MANAGER_DIR, "[MANAGER_SYS_ROOT_DIR]", MANAGER_SYS_ROOT_DIR);
			} else {
				absolutePath =  StringUtil.replace(DIAG_UNIX_MANAGER_DIR, "[MANAGER_SYS_ROOT_DIR]", MANAGER_SYS_ROOT_DIR);
			}
		} else {
			absolutePath =  StringUtil.replace(DIAG_FILE_MANAGER_DIR, "[MANAGER_SYS_ROOT_DIR]", MANAGER_SYS_ROOT_DIR);
		}

		if (os != null && os.toUpperCase().contains(WIN_ID)) {
			absolutePath = StringUtil.replace(absolutePath, "[SLASH]", PATH_SLASH_WIN);
		}
		else {
			absolutePath = StringUtil.replace(absolutePath, "[SLASH]", PATH_SLASH_UNIX);
		}

		return absolutePath;
	}

	public static String scriptFileAbsolutePathDiagInfoNotUse(String jobType, String osType) {
		String absolutePath;
		String os = System.getProperty("os.name");

		if (jobType.equalsIgnoreCase(ManagerJobType.AJ100.toString())) {
			if (osType.toUpperCase().contains(INMEMORYDB.WIN_ID)) {
				absolutePath =  StringUtil.replace(DIAG_WINDOWS_MANAGER_DIR, "[MANAGER_SYS_ROOT_DIR]", MANAGER_SYS_ROOT_DIR);
			} else {
				absolutePath =  StringUtil.replace(DIAG_UNIX_MANAGER_DINFONOTUSE_DIR, "[MANAGER_SYS_ROOT_DIR]", MANAGER_SYS_ROOT_DIR);
			}
		} else {
			absolutePath =  StringUtil.replace(DIAG_FILE_MANAGER_DIR, "[MANAGER_SYS_ROOT_DIR]", MANAGER_SYS_ROOT_DIR);
		}

		if (os != null && os.toUpperCase().contains(WIN_ID)) {
			absolutePath = StringUtil.replace(absolutePath, "[SLASH]", PATH_SLASH_WIN);
		}
		else {
			absolutePath = StringUtil.replace(absolutePath, "[SLASH]", PATH_SLASH_UNIX);
		}

		return absolutePath;
	}

	public static String diagnoisResultAbsolutePath() {
		String absolutePath = StringUtil.replace(DIAG_RESULT_FILE_BACKUP, "[MANAGER_SYS_ROOT_DIR]", MANAGER_SYS_ROOT_DIR);
		String os = System.getProperty("os.name");

		if (os != null && os.toUpperCase().contains(WIN_ID)) {
			absolutePath = StringUtil.replace(absolutePath, "[SLASH]", PATH_SLASH_WIN);
		}
		else {
			absolutePath = StringUtil.replace(absolutePath, "[SLASH]", PATH_SLASH_UNIX);
		}

		return absolutePath;
	}

	public static void deleteShFile(JobEntity jobEntity, String pathType) {
		try {
			String fileName = jobEntity.getFileName();
			String path = INMEMORYDB.jobTypeAbsolutePath(pathType, jobEntity.getJobType());
			File file = new File(path);
				for(File f:file.listFiles()) {
					if(f.getName().contains(fileName)) {
						f.delete();
					}
			}
		}catch (NullPointerException e){}
	}

	public static void deleteFile(JobEntity jobEntity, String pathType) {
		try {
			String fileName = jobEntity.getFileName();
			String path = INMEMORYDB.jobTypeAbsolutePath(pathType, jobEntity.getJobType());
			File file = new File(path);
			for(File f:file.listFiles()) {
				if(f.getName().contains(fileName)) {
					f.delete();
				}
			}
		}catch (NullPointerException e){}
	}

	public static void deleteLog4JFiles(String Otp) {

		String sendPath = jobTypeAbsolutePath(INMEMORYDB.SEND, ManagerJobType.AJ200.toString());
		CommonUtils.deleteDirectory(new File(sendPath + Otp));
	}

	public static void createRunningGsJobList(JobEntity jobEntity) {
		INMEMORYDB.updateJobListPerAsset(jobEntity.getAssetCd(), jobEntity.getJobType());

		RunnigJobEntity runnigJobEntity = new RunnigJobEntity(DateUtil.getCurrDateBySecond());
		runnigJobEntity.setJobEntity(jobEntity);
		INMEMORYDB.RUNNINGGSJOBLIST.put(jobEntity.getAssetCd(), runnigJobEntity);
	}

	public static void createRunningDgJobList(Dao dao, JobEntity jobEntity) throws Exception {
		// SKT의 경우 현재 10명(1명당 1개, 즉 10개) 이상의 진단 실행 요청을 받지 못한다.
		if ((INMEMORYDB.maxDGreq > 0) && (INMEMORYDB.RUNNINGDGJOBLIST.size() >= INMEMORYDB.maxDGreq)) {
			if (!INMEMORYDB.RUNNINGDGJOBLIST.containsKey(jobEntity.getAssetCd())) {
				String errMsg = "Users tries to perform too many requests. (a limit of" + INMEMORYDB.maxDGreq + ")";
				throw new SnetException(dao, errMsg , jobEntity, "D");
			}
		}

		INMEMORYDB.updateJobListPerAsset(jobEntity.getAssetCd(), jobEntity.getJobType());

		RunnigJobEntity runnigJobEntity = new RunnigJobEntity(DateUtil.getCurrDateBySecond());
		runnigJobEntity.setJobEntity(jobEntity);
		INMEMORYDB.RUNNINGDGJOBLIST.putIfAbsent(jobEntity.getAssetCd(),runnigJobEntity);
	}

	public static void createRunningNwJobList(JobEntity jobEntity) {
		INMEMORYDB.updateJobListPerAsset(jobEntity.getAssetCd(), jobEntity.getJobType());

		RunnigJobEntity runnigJobEntity = new RunnigJobEntity(DateUtil.getCurrDateBySecond());
		runnigJobEntity.setJobEntity(jobEntity);
		INMEMORYDB.RUNNINGNWJOBLIST.put(jobEntity.getAssetCd(),runnigJobEntity);
	}

	public static void createRunningLogJobList(JobEntity jobEntity) {
		INMEMORYDB.updateJobListPerAsset(jobEntity.getAssetCd(), jobEntity.getJobType());

		RunnigJobEntity runnigJobEntity = new RunnigJobEntity(DateUtil.getCurrDateBySecond());
		runnigJobEntity.setJobEntity(jobEntity);
		INMEMORYDB.RUNNINGLOGJOBLIST.put(jobEntity.getAssetCd(),runnigJobEntity);
	}

	public static void createRunningSetupJobList(JobEntity jobEntity) {
		INMEMORYDB.updateJobListPerAsset(jobEntity.getAssetCd(), jobEntity.getJobType());

		RunnigJobEntity runnigJobEntity = new RunnigJobEntity(DateUtil.getCurrDateBySecond());
		runnigJobEntity.setJobEntity(jobEntity);
		INMEMORYDB.RUNNINGSETUPJOBLIST.put(jobEntity.getAssetCd(),runnigJobEntity);
	}

	public static void createRunningControlJobList(JobEntity jobEntity) {
		INMEMORYDB.updateJobListPerAsset(jobEntity.getAssetCd(), jobEntity.getJobType());

		RunnigJobEntity runnigJobEntity = new RunnigJobEntity(DateUtil.getCurrDateBySecond());
		runnigJobEntity.setJobEntity(jobEntity);
		INMEMORYDB.RUNNINGCONTROLJOBLIST.put(jobEntity.getAssetCd(),runnigJobEntity);
	}

	public static void createSshMem(JobEntity jobEntity) {
		RunnigJobEntity runnigJobEntity = new RunnigJobEntity(DateUtil.getCurrDateByminute());
		runnigJobEntity.setMsg("running ssh service.");
		runnigJobEntity.setJobEntity(jobEntity);
		INMEMORYDB.RUNNING_SSHJOBLIST.put(jobEntity.getAssetCd(), runnigJobEntity);
	}

	public static void removeJOBAPIList(String assetCd) {
		try {
			INMEMORYDB.AGENTSCHEDULEASSETCHECKLIST.remove(assetCd);
			int chkTime = 0;
			if (INMEMORYDB.AGENTSCHEDULEASSETLIST.containsKey(assetCd)) {
				chkTime = INMEMORYDB.AGENTSCHEDULEASSETLIST.get(assetCd);
				INMEMORYDB.AGENTSCHEDULEASSETLIST.remove(assetCd);
			}

			CopyOnWriteArrayList<String> assetCdList = INMEMORYDB.AGENTSCHEDULELIST.get(chkTime);
			if (assetCdList != null) {
				for(String checkCd : assetCdList) {
					if (checkCd.equals(assetCd)) {
						assetCdList.remove(checkCd);
						break;
					}
				}
			}
		} catch (NullPointerException e) {}

	}

	public static void removeDGJOBList(JobEntity jobEntity) {
		try {
			INMEMORYDB.RUNNINGDGJOBLIST.remove(jobEntity.getAssetCd());
		} catch (NullPointerException e) {}

	}

	public static void removeGSJOBList(JobEntity jobEntity) {
		try {
			INMEMORYDB.RUNNINGGSJOBLIST.remove(jobEntity.getAssetCd());
		} catch (NullPointerException e) {}

	}

	public static void removeNWJOBList(JobEntity jobEntity) {
		try {
			INMEMORYDB.RUNNINGNWJOBLIST.remove(jobEntity.getAssetCd());
		} catch (NullPointerException e) {}

	}

	public static void removeLOGJOBList(JobEntity jobEntity) {
		try {
			INMEMORYDB.RUNNINGLOGJOBLIST.remove(jobEntity.getAssetCd());
		} catch (NullPointerException e) {}

	}

	public static void removeSETUPJOBList(JobEntity jobEntity) {
		try {
			INMEMORYDB.RUNNINGSETUPJOBLIST.remove(jobEntity.getAssetCd());
		} catch (NullPointerException e) {}

	}

	public static void removeCONTROLJOBList(JobEntity jobEntity) {
		try {
			INMEMORYDB.RUNNINGCONTROLJOBLIST.remove(jobEntity.getAssetCd());
		} catch (NullPointerException e) {}

	}

	public static void removeSshJobList(JobEntity jobEntity) {
		try {
			INMEMORYDB.RUNNING_SSHJOBLIST.remove(jobEntity.getAssetCd());
		} catch (NullPointerException e){}

	}

	public static void removeJobListPerAsset(String assetCd, String jobType){
		try {
			CopyOnWriteArrayList<AgentJob> curJobList = INMEMORYDB.JOBLISTPERASSET.get(assetCd);
			if (curJobList != null) {
				for (AgentJob aj : curJobList) {
					if (aj.getJobType().equals(jobType)) {
						curJobList.remove(aj);
						break;
					}
				}
				if (curJobList.size() == 0) {
					INMEMORYDB.JOBLISTPERASSET.remove(assetCd);
				}
			}
		} catch (NullPointerException e){}
	}

	public static void updateJobListPerAsset(String assetCd, String jobType){

		boolean exist = false;
		String sched = Integer.toString(DefaultJobSchedule);
		AgentJob agentJob = new AgentJob(assetCd, jobType, "0", agentJobCheckCnt, sched);  //기본 5분 delaytime 설정
		AgentJob existJob = null;
		CopyOnWriteArrayList<AgentJob> curJobList = INMEMORYDB.JOBLISTPERASSET.get(assetCd);
		if (curJobList == null) {
			curJobList = new CopyOnWriteArrayList<AgentJob>();
		} else {
			for (AgentJob aj : curJobList) {
				if (aj.getJobType().equals(jobType) ) {
					exist = true;
					existJob = aj;
					break;
				}
			}
		}

		if (!exist) {
			curJobList.add(agentJob);
			INMEMORYDB.JOBLISTPERASSET.putIfAbsent(assetCd, curJobList);
		} else {
			existJob.setCheckFlag("0");
		}
	}

	public static void updateGSMem(JobEntity jobEntity) {

		RunnigJobEntity runnigJobEntity = INMEMORYDB.RUNNINGGSJOBLIST.get(jobEntity.getAssetCd());
		runnigJobEntity.setJobEntity(jobEntity);
		INMEMORYDB.RUNNINGGSJOBLIST.put(jobEntity.getAssetCd(),runnigJobEntity);
	}

	public static void updateDGMem(JobEntity jobEntity) {

		RunnigJobEntity runnigJobEntity = INMEMORYDB.RUNNINGDGJOBLIST.get(jobEntity.getAssetCd());
		runnigJobEntity.setJobEntity(jobEntity);
		INMEMORYDB.RUNNINGDGJOBLIST.put(jobEntity.getAssetCd(),runnigJobEntity);
	}

	public static void updateNWMem(JobEntity jobEntity) {
		RunnigJobEntity runnigJobEntity = INMEMORYDB.RUNNINGNWJOBLIST.get(jobEntity.getAssetCd());
		runnigJobEntity.setJobEntity(jobEntity);
		INMEMORYDB.RUNNINGNWJOBLIST.put(jobEntity.getAssetCd(),runnigJobEntity);
	}

	public static void updateLOGMem(JobEntity jobEntity) {
		RunnigJobEntity runnigJobEntity = INMEMORYDB.RUNNINGLOGJOBLIST.get(jobEntity.getAssetCd());
		runnigJobEntity.setJobEntity(jobEntity);
		INMEMORYDB.RUNNINGLOGJOBLIST.put(jobEntity.getAssetCd(),runnigJobEntity);
	}

	public static void updateSETUPMem(JobEntity jobEntity) {

		RunnigJobEntity runnigJobEntity = INMEMORYDB.RUNNINGSETUPJOBLIST.get(jobEntity.getAssetCd());
		runnigJobEntity.setJobEntity(jobEntity);
		INMEMORYDB.RUNNINGSETUPJOBLIST.put(jobEntity.getAssetCd(),runnigJobEntity);
	}

	public static void updateCONTROLMem(JobEntity jobEntity) {
		RunnigJobEntity runnigJobEntity = INMEMORYDB.RUNNINGCONTROLJOBLIST.get(jobEntity.getAssetCd());
		runnigJobEntity.setJobEntity(jobEntity);
		INMEMORYDB.RUNNINGCONTROLJOBLIST.put(jobEntity.getAssetCd(),runnigJobEntity);
	}

	public static void main(String args[]) {
		SETUP_FILE_MANAGER_DIR = replaceDir(SETUP_FILE_MANAGER_DIR,"XXXX");
		System.out.println(SETUP_FILE_MANAGER_DIR);
		
	}

}
