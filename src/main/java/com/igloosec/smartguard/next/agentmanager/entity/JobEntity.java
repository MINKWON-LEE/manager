/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.entity.JobEntity.java
 * company : Mobigen
 * @author : Oh su jin
 * created at : 2016. 2. 19.
 * description :
 */
package com.igloosec.smartguard.next.agentmanager.entity;

import lombok.Data;

import java.io.File;

@Data
public class JobEntity extends BaseDBEntity {

	String sOTP; // JOB 고유 아이디로 사용
	String cOTP; // Agent OTP Code

	// Agent_Job_History
	String swType; // OS,DB,WAS,WEB..
	String swNm; // LINUX,WINDOW,ORACLE,JEUS...
	String swInfo; // Version info
	String swDir;
	String swUser;
	String swEtc;
	Integer auditType; // 1:테스트, 2:정식 수행
	String agentJobRdate; // 진단 요청시간
	String auditFileName; // 진단스크립트 경로+파일 명
	String checksumHash; // 진단스크립트 해쉬값
	String diagInfoChecksumHash; // 환경파일 수집 스크립트 해쉬값
    String infoChecksumHash; // 장비정보 수집스크립트 해쉬값
	Integer agentJobFlag; // 0:진단대기 , 1:진단요청, 2:진단시작, 3:진단 완료, 4:작업실패
	String agentJobSDate; // 진단 시작시간
	String agentJobEDate; // 진단 종료시간
	String auditFileCd; // 진단 파일 명
	String auditFileType; // 진단 기준
	String reqType; // getscript,diag,setup,test
	String jobType; // OTP, GSCRPTFILE, DGFILE
	String fileType; // .otp , .jar , .sh
	String sDate; // Manager쪽에서 OTP 발급한 시간.
	String cDate;
	String fileName; // 난수 값으로 생성된 파일 명.
	String hostNm;
	File[] files; //여러 파일을 보내는 JOB 용 파일들 //i.e. Agent Update
	String auditSpeed;
	String globalPwl;
	String globalSts;
	
	//EVENT 
	String prgId; // 이벤트 진단 프로그램 아이디


	// 진단작업 수행관련 필요 설명자료
	String agentJobDesc;
	

	// 진단 결과에 추가될 Manager Code
	String managerCode;

	//수동업로드 assetCd,ipAddres
	String assetCd;
	String ipAddres;

	String agentCd;

	//2018.12.19 진단로그
	String statusLog;

	//kill agent
	boolean killAgent = false;

	AgentInfo agentInfo;

	AgentInfo relayAgentInfo;
	
	AgentInfo relay2AgentInfo;
	
	AgentInfo relay3AgentInfo;

	//네트워크 장비 config 명령어 호출
	String nwCMD;

	//Agent를 통한 get 프로그램 flag
	boolean isAgentGetprog = false;

	String eventFlag = "N";

	boolean isAgentManualSetup = false;

	String hashTxt;        // 여러 파일을 압축하여 전송할 때 각각의 파일의 해쉬값을 적어놓은 파일 이름.

	String govFlag;

	// job 요청 id
	String userId;

	String userNm;

	String getForDiag;

	String passWordType;

	String passWordOptionType;

	// AJ100 일때 진단실패에 따른 로그파일만 업로드
	boolean uploadLogOnly = false;
	String uploadLogOnlyPath;

	/**
	 * 수집 진단 분리
	 */
	//  수집 / 진단 분리 사용여부
	String diagInfoUse = "N";
	// 수집 / 진단 분리시 수집된 환경파일을 진단하기 위한 디렉터리.
	String diagDir;
	// 수집 / 진단 분리시 진단완료 후 환경 수집 파일 백업 위치
	String diagInfoFilePath;
	// 수집 / 진단 분리시 수집된 환경파일을 UI 다운로드 가능한 ZIP 파일 생성 플래그
	Integer diagInfoFlag;  // 1. 요청 2. 완료 3. 실패 4. 진단실행만 완료상태
	// 수집 / 진단 분리시 수집된 환경파일을 UI 다운로드 가능한 ZIP 파일 경로
	String diagInfoFilePathDes;
	// 수집 / 진단 분리시 로그
	String diagInfoJobDesc;
	// 수집 / 진단 분리시 현재 서버에서 진단실행 중인지 체크 (DiagnosisUtil 클래스)
	// false : 진단 실행 중이지 않음.  true : 진단 실행 중
	boolean diagInfoHandling = false;

	int getSeq;
}
