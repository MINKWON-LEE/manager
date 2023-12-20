/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.services.DiagnosisManager.java
 * company : Mobigen
 *
 * @author : Je Joong Lee
 * created at : 2016. 2. 23.
 * description :
 */

package com.igloosec.smartguard.next.agentmanager.services;

import com.igloosec.smartguard.next.agentmanager.crypto.AESCryptography;
import com.igloosec.smartguard.next.agentmanager.dao.Dao;

import com.igloosec.smartguard.next.agentmanager.entity.AgentInfo;
import com.igloosec.smartguard.next.agentmanager.entity.ConfigAuditItemOption;
import com.igloosec.smartguard.next.agentmanager.entity.JobEntity;
import com.igloosec.smartguard.next.agentmanager.entity.RunnigJobEntity;
import com.igloosec.smartguard.next.agentmanager.exception.CustomDiagDataParseException;
import com.igloosec.smartguard.next.agentmanager.exception.CustomDiagValidationException;
import com.igloosec.smartguard.next.agentmanager.exception.SnetCommonErrCode;
import com.igloosec.smartguard.next.agentmanager.exception.SnetException;
import com.igloosec.smartguard.next.agentmanager.memory.INMEMORYDB;
import com.igloosec.smartguard.next.agentmanager.memory.ManagerJobFactory;
import com.igloosec.smartguard.next.agentmanager.memory.ManagerJobType;
import com.igloosec.smartguard.next.agentmanager.property.AgentContextProperties;
import com.igloosec.smartguard.next.agentmanager.property.FileStorageProperties;
import com.igloosec.smartguard.next.agentmanager.property.OptionProperties;
import com.igloosec.smartguard.next.agentmanager.utils.*;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DiagnosisManager {

    private Dao dao;
    private DataParseManager dataParseManager;
    private ConnectionManager connectionManager;
    private CustomDiagnosisService customDiagnosisService;
    private FileStorageProperties fileStorageProperties;
    private OptionProperties optionProperties;
    private AgentContextProperties agentContextProperties;

    public DiagnosisManager(Dao dao, DataParseManager dataParseManager,
                            ConnectionManager connectionManager, CustomDiagnosisService customDiagnosisService,
                            FileStorageProperties fileStorageProperties, OptionProperties optionProperties,
                            AgentContextProperties agentContextProperties) {
        this.dao = dao;
        this.dataParseManager = dataParseManager;
        this.connectionManager = connectionManager;
        this.customDiagnosisService = customDiagnosisService;
        this.fileStorageProperties = fileStorageProperties;
        this.optionProperties = optionProperties;
        this.agentContextProperties = agentContextProperties;
    }

    public String doDiagnosisService_V3(JobEntity jobEntity) throws Exception {
        int errorStep = 0;
        try {
            log.debug("[" + jobEntity.getAssetCd() + "] Start Diagnosis Send");

            if (jobEntity.getSwNm().toUpperCase().contains(INMEMORYDB.WIN_ID)
                    || jobEntity.getSwNm().toUpperCase().contains("IIS")
                    || jobEntity.getSwNm().toUpperCase().contains("MSSQL")
                    || jobEntity.getAuditFileName().toUpperCase().endsWith(".ZIP") ) {
                jobEntity.setFileType(INMEMORYDB.ZIP);
            } else {
                jobEntity.setFileType(INMEMORYDB.SH);
            }

            //Diagnosis Script create & if:DB create db password file.
            if(jobEntity.getJobType().equals(ManagerJobType.AJ101.toString()) || jobEntity.getEventFlag().equalsIgnoreCase("Y")){
                createEventDiagnosis(jobEntity);
            } else {
                createDiagnosis_java14(jobEntity);
            }

            /**
             * 진단 스크립트 압축파일 암호화
             */
            //File encryption
            File[] files = null;
            files = new AESCryptography().encryptionFile(jobEntity);

            if(optionProperties.getDiagnosis().equals("false")) {
                String delzip = INMEMORYDB.jobTypeAbsolutePath(INMEMORYDB.SEND, jobEntity.getJobType()) + jobEntity.getFileName() + ".zip";
                CommonUtils.deleteFile(delzip);
            }
            /**
             * 수집 진단 분리시 수집된 환경파일을 진단하기 위한 디렉터리 생성.
             */
            if (jobEntity.getDiagInfoUse().toLowerCase().equals("y") && !INMEMORYDB.diagInfoNotUse) {
                String dir = INMEMORYDB.DIAG_INFO_FILE_MANAGER_DIR + jobEntity.getAssetCd();
                CommonUtils.mkdir(dir);
            }

            errorStep = 1;

            return files[0].getAbsolutePath();
        } catch (Exception e) {
            INMEMORYDB.deleteFile(jobEntity, INMEMORYDB.SEND);
            log.error(e.getMessage(), e.fillInStackTrace());
            if (errorStep == 0) {
                throw new Exception(e.getMessage() + "(" + SnetCommonErrCode.ERR_0017.getMessage() + ")");
            } else {
                throw new Exception(e.getMessage());
            }
        }
    }

	@SuppressWarnings({ "unused", "static-access" })
	public void createDiagnosis_java14(JobEntity jobEntity) throws Exception {
		String diagsFileName;
		String diagsOrgiFilePath;
		boolean isDiagDecryption = false;
		boolean isGetDecryption = false;

		ArrayList<File> file = new ArrayList<File>();

		log.debug("진단 파일 : "+jobEntity.getAuditFileName());

		diagsFileName = jobEntity.getAuditFileName();


		diagsOrgiFilePath = INMEMORYDB.scriptFileAbsolutePath(jobEntity.getJobType(),jobEntity.getAgentInfo().getOsType());

        // 수집 진단 분리시
        // 수집 진단 분리된 스크립트 오류 있을 경우 대비 임시로 원래의 진단 스크립트를 보내주는 옵션 처리
		if (jobEntity.getDiagInfoUse().toLowerCase().equals("y") && INMEMORYDB.diagInfoNotUse) {
		    log.debug("send diagnosis tmp file. but check origin diagnosis file by using diaginfoUse.");
            diagsOrgiFilePath = INMEMORYDB.scriptFileAbsolutePathDiagInfoNotUse(jobEntity.getJobType(),jobEntity.getAgentInfo().getOsType());
        }
		String diagnosisFileFullPath = diagsOrgiFilePath + diagsFileName;

		String dgFilePath = "";

		if(jobEntity.getAgentInfo().getOsType().toUpperCase().contains(INMEMORYDB.WIN_ID)){
			dgFilePath = INMEMORYDB.jobTypeAbsolutePath(INMEMORYDB.SEND, jobEntity.getJobType()) + jobEntity.getSOTP() +"_"+ jobEntity.getSwType() +jobEntity.getFileType();
		}else {
			dgFilePath = INMEMORYDB.jobTypeAbsolutePath(INMEMORYDB.SEND, jobEntity.getJobType()) + jobEntity.getSOTP() + jobEntity.getFileType();
		}

		String gsOrigiFilePath = INMEMORYDB.GETSCRIPT_UNIX_MANAGER_DIR + CommonUtils.toMux(INMEMORYDB.diagGetUnixAgent);
		String gsFilesPath = INMEMORYDB.jobTypeAbsolutePath(INMEMORYDB.SEND, jobEntity.getJobType()) + jobEntity.getSOTP() + jobEntity.getGetForDiag() + jobEntity.getFileType();

		log.debug("original diagnosis : " + diagnosisFileFullPath);
		log.debug("copy diagnosis : " + dgFilePath);

		/** decryption File **/
		diagsFileName = CommonUtils.toMux(diagsFileName);
//		log.debug("to Mux : "+diagsFileName);
		diagnosisFileFullPath = diagsOrgiFilePath + diagsFileName;

		// 수집 / 진단 분리 기능 확인
        if (jobEntity.getDiagInfoUse().toLowerCase().equals("y") && !INMEMORYDB.diagInfoNotUse) {
            dgFilePath = INMEMORYDB.jobTypeAbsolutePath(INMEMORYDB.SEND, jobEntity.getJobType()) + jobEntity.getSOTP() + jobEntity.getFileType();

            if(!jobEntity.getAgentInfo().getOsType().toUpperCase().contains(INMEMORYDB.WIN_ID)) {
                diagnosisFileFullPath = INMEMORYDB.GETSCRIPT_UNIX_MANAGER_DIR + CommonUtils.toMux(INMEMORYDB.diagInfoAgent);
            } else {
                diagnosisFileFullPath = INMEMORYDB.GETSCRIPT_WINDOWS_MANAGER_DIR + CommonUtils.toMux(INMEMORYDB.diagInfoAgent);
            }
            log.debug("diagInfoAgent for Mux : " + diagnosisFileFullPath);
        }
        isDiagDecryption = new AESCryptography().decryptionShFile(diagnosisFileFullPath,dgFilePath);

		if (isDiagDecryption){
            // 정상적으로 파일 decrypt되면 해당 파일의 hash값을 뜨고 db에 조회하여 값이 맞는지 확인 후 진행 다를 경우 익셉션 처리.
            AESCryptography aesCryptography = new AESCryptography();
            String hash = aesCryptography.encryption(Paths.get(dgFilePath));
            log.debug("checksum hash : " + hash);

            if (jobEntity.getDiagInfoUse().toLowerCase().equals("y") && !INMEMORYDB.diagInfoNotUse) {
                log.debug("checksum hash db : " + jobEntity.getDiagInfoChecksumHash());
                if (!hash.equals(jobEntity.getDiagInfoChecksumHash())) {
                    log.error("Diagnosis File Hash mismatch.");
                    throw new Exception(SnetCommonErrCode.ERR_0037.getMessage());
                }
            } else {
                log.debug("checksum hash db : " + jobEntity.getChecksumHash());
                if (!hash.equals(jobEntity.getChecksumHash())){
                    log.error("Diagnosis File Hash mismatch.");
                    throw new Exception(SnetCommonErrCode.ERR_0037.getMessage());
                }
            }

			if(!jobEntity.getAgentInfo().getOsType().toUpperCase().contains(INMEMORYDB.WIN_ID)){
				//get 프로그램 복사
                isGetDecryption = new AESCryptography().decryptionShFile(gsOrigiFilePath,gsFilesPath);
                hash = aesCryptography.encryption(Paths.get(gsFilesPath));
                log.debug("get script checksum hash : " + hash);
                if (!hash.equals(jobEntity.getInfoChecksumHash())){
                    log.error("get File Hash mismatch.");
                    throw new Exception(SnetCommonErrCode.ERR_0036.getMessage());
                }
			}else if(jobEntity.getAgentInfo().getOsType().toUpperCase().contains(INMEMORYDB.WIN_ID)){
				//make dummy File
                byte[] salt = new byte[8];
                FileOutputStream saltOutFile = new FileOutputStream(gsFilesPath);
                saltOutFile.write(salt);
                saltOutFile.close();
			}

            /** 진단 파일을 전달할때는 무조건 zip 으로 묶어서 전달한다. **/
            jobEntity.setFileType(INMEMORYDB.ZIP);

            String pwFile = makeConfig(jobEntity);
            String pwOptionFile = "fail";
            if (INMEMORYDB.useDiagOption) {
                pwOptionFile = makeDiagnosisOptionConfig(jobEntity);
            }

            file.add(new File(gsFilesPath));
            file.add(new File(dgFilePath));

            if (!pwFile.equalsIgnoreCase("fail"))
                file.add(new File(pwFile));

            if (!pwOptionFile.equalsIgnoreCase("fail"))
                file.add(new File(pwOptionFile));


            new ZipUtil().makeZip(file, jobEntity);

            if(optionProperties.getDiagnosis().equals("false")) {
                Thread.sleep(5);

                if (!jobEntity.getDiagInfoUse().toLowerCase().equals("y") && !INMEMORYDB.diagInfoNotUse) {
                    CommonUtils.deleteFile(gsFilesPath);
                    CommonUtils.deleteFile(dgFilePath);
                    CommonUtils.deleteFile(pwFile);
                    if (!pwFile.equalsIgnoreCase("fail"))
                        CommonUtils.deleteFile(pwOptionFile);
                }
            }
        } else {
            log.error("Diagnosis Decryption Fail.");
            throw new Exception(SnetCommonErrCode.ERR_0006.getMessage());
        }
    }

    @SuppressWarnings("static-access")
	public void createEventDiagnosis(JobEntity jobEntity) throws Exception {
        ArrayList<File> file = new ArrayList<>();
        String diagsFileName = jobEntity.getAuditFileName();
        
        log.debug("EVENT_DG_FILE : {}", diagsFileName);

        //2018.12.10 이상준
		//긴급진단일 경우 PRG_NM 전송을 위하여 ManagerCode 컬럼 사용(에이전트 전송하기위한 로직)
		jobEntity.setManagerCode(diagsFileName);

        String eventDiagFile    = INMEMORYDB.EVENT_DIAG_SCRIPT + diagsFileName;
        String cpEventDiagFile  = INMEMORYDB.jobTypeAbsolutePath(INMEMORYDB.SEND, jobEntity.getJobType()) + jobEntity.getSOTP() + agentContextProperties.getDiagForEvent() + jobEntity.getFileType();

        String eventSetupFile   = INMEMORYDB.DIAG_UNIX_MANAGER_DIR + CommonUtils.toMux(INMEMORYDB.diagEventUnixAgent);
        // Decrypt file name 
        String cpEventSetupFile = INMEMORYDB.jobTypeAbsolutePath(INMEMORYDB.SEND, jobEntity.getJobType()) + jobEntity.getSOTP() +jobEntity.getFileType();


        log.debug("ORG_EVENT_DG_FILE :: {} ", eventDiagFile);
        log.debug("SEND_EVENT_DG_FILE :: {} : ", cpEventDiagFile);

        log.debug("eventSetupFile :: {} ", eventSetupFile);
        log.debug("cpEventSetupFile :: {} : ", cpEventSetupFile);


        // Event 진단 사용자 정의 파일 copy
        FileUtils.fileCopy(eventDiagFile, cpEventDiagFile);


        /** 진단 파일을 전달할때는 무조건 zip 으로 묶어서 전달한다. **/
        jobEntity.setFileType(INMEMORYDB.ZIP);

        /*
         * 설정 파일 decryption
         */
        new AESCryptography().decryptionShFile(eventSetupFile ,cpEventSetupFile);

        /** 진단 파일을 전달할때는 무조건 zip 으로 묶어서 전달한다. **/
        jobEntity.setFileType(INMEMORYDB.ZIP);

        file.add(new File(cpEventSetupFile));
        file.add(new File(cpEventDiagFile));
        new ZipUtil().makeZip(file, jobEntity);
    }

    @SuppressWarnings({ "unused", "static-access" })
    public void createDiagnosisForSvr_java14(JobEntity jobEntity) throws Exception {

        log.debug("진단 파일 : "+jobEntity.getAuditFileName());

        String diagsFileName = jobEntity.getAuditFileName();


        String diagsOrgiFilePath = INMEMORYDB.scriptFileAbsolutePath(jobEntity.getJobType(),jobEntity.getAgentInfo().getOsType());
        String diagnosisFileFullPath = diagsOrgiFilePath + diagsFileName;

        String dgFilePath = "";

        if(jobEntity.getAgentInfo().getOsType().toUpperCase().contains(INMEMORYDB.WIN_ID)){
            dgFilePath = INMEMORYDB.jobTypeAbsolutePath(INMEMORYDB.SEND, jobEntity.getJobType()) + jobEntity.getSOTP() +"_"+ jobEntity.getSwType() + jobEntity.getFileType();
        }else {
            dgFilePath = INMEMORYDB.jobTypeAbsolutePath(INMEMORYDB.SEND, jobEntity.getJobType()) + jobEntity.getSOTP() + "_svr"+  INMEMORYDB.SH;
        }

        log.debug("original diagnosis : " + diagnosisFileFullPath);
        log.debug("copy diagnosis : " + dgFilePath);

        /** decryption File **/
        diagsFileName = CommonUtils.toMux(diagsFileName);
//		log.debug("to Mux : "+diagsFileName);
        diagnosisFileFullPath = diagsOrgiFilePath + diagsFileName;

        boolean isDiagDecryption = new AESCryptography().decryptionShFile(diagnosisFileFullPath,dgFilePath);

        if (isDiagDecryption){
            log.debug("Diagnosis Decryption Success.");
        } else {
            log.error("Diagnosis Decryption Fail.");
            throw new Exception(SnetCommonErrCode.ERR_0006.getMessage());
        }
    }

    //수동으로 진단 스크립트 결과를 업로드를 수행하여 받은 결과 파일
    public void recvManualDGscriptResult(String filePath, JobEntity jobEntity) throws SnetException, IOException, InterruptedException {

        String upladPath;

		jobEntity.setAgentJobSDate(DateUtil.getCurrDateBySecond());
		jobEntity.setAgentJobDesc("수동업로드 진단시작");
		jobEntity.setAgentJobFlag(2);
		log.debug("=======dignosisManager recvManualDGscriptResult Start=========");

		if (jobEntity.getJobType().equals(ManagerJobType.WM102.toString())) {
            log.debug("=======dignosisManager recvManualDGscriptResult Start=========");
            dao.updateAgentJobHistory(jobEntity);
        } else {
            log.debug("=======dignosisManager recvManualDGscriptResult Multi Start=========");
        }

        if (filePath != null) {
            // window에서 개발시 필요.
            if(optionProperties.getDiagnosis().equals("true") && filePath.contains("/")) {
                filePath = CommonUtils.replacePathSeperator(filePath);
            }
            upladPath = filePath + File.separator;
        } else {
            upladPath = INMEMORYDB.MANUAL_DIAGR;
        }
        upladPath += jobEntity.getFileName();

        String desPath = "", xmlPath = "", movePath = "", moveXmlPath = "";
        desPath += upladPath + INMEMORYDB.DESRESULTTYPE;
        xmlPath += upladPath + jobEntity.getFileType();

        movePath = INMEMORYDB.jobTypeAbsolutePath(INMEMORYDB.RECV, jobEntity.getJobType()) + jobEntity.getFileName();
        moveXmlPath += movePath + jobEntity.getFileType();

        log.debug("upladPath : " + upladPath);
        log.debug("movePath : " + movePath);

        log.debug("getAssetCd : " + jobEntity.getAssetCd());

        String failLog = "Manual Diagsnosis File upload Fail.";

        boolean isMoved;
        File file = new File(desPath);
        if (file.exists()) {
            try {
                isMoved = new AESCryptography().decryptionShFile(desPath, moveXmlPath);
            } catch (Exception e) {
                throw new SnetException(failLog);
            }
        } else {
            isMoved = CommonUtils.fileCopy(xmlPath, moveXmlPath);
        }

        if (isMoved) {

            try {

                //EUC-KR convert to UTF-8
                CommonUtils.convertUTF8(jobEntity);

                /**진단 결과파일 파싱*/
                dataParseManager.dscriptResult(jobEntity);

				/*
                 * Agent Master 설치 정보 업데이트
				 * 4 미설치
				 */
				// Agnet 수동업로드시 미설치로 변경되는 로직 주석 처리 (에이전트가 설치되있으면 그대로 유지)
                //dao.deleteAgentMaster(jobEntity);

                //진단결과 백업
                doDiagnosisResultBackup(jobEntity);

                //Delete GetScript result File.
                INMEMORYDB.deleteFile(jobEntity, INMEMORYDB.RECV);

				jobEntity.setAgentJobDesc("수동업로드 진단완료");
				jobEntity.setAgentJobFlag(3);
				jobEntity.setAgentJobEDate(DateUtil.getCurrDateBySecond());
				log.debug("=======dignosisManager recvManualDGscriptResult DGFin=========");

                if (jobEntity.getJobType().equals(ManagerJobType.WM102.toString())) {
                    dao.updateAgentJobHistory(jobEntity);
                }
                log.info("Manual Diagsnosis is done.");
//            } catch (MalformedByteSequenceException e) {
//                log.error(CommonUtils.printError(e));
//                throw new SnetException(SnetCommonErrCode.ERR_0015.getMessage());
            } catch (IOException e) {
            	log.error(e.toString(), e);  // log.error(CommonUtils.printError(e));
                throw new IOException(SnetCommonErrCode.ERR_0034.getMessage());
            } catch (ParserConfigurationException e) {
            	log.error(e.toString(), e);  // log.error(CommonUtils.printError(e));
                throw new SnetException(SnetCommonErrCode.ERR_0015.getMessage());
            } catch (SAXException e) {
            	log.error(e.toString(), e);  // log.error(CommonUtils.printError(e));
                throw new SnetException(SnetCommonErrCode.ERR_0015.getMessage());
            } catch (SnetException e) {
            	log.error(e.toString(), e);  // log.error(CommonUtils.printError(e));
                throw new SnetException(SnetCommonErrCode.ERR_0014.getMessage());
            } catch (Exception e) {
            	log.error(e.toString(), e);  // log.error(CommonUtils.printError(e));
                throw new SnetException(e.getMessage());
            }

        } else {
            log.error(failLog);
            throw new SnetException(failLog);
        }
    }
      public void recvEventDGscriptResult(JobEntity jobEntity) throws Exception {
        
        log.debug("Send DGFIN and Receive Get Script result file");
        try {
            jobEntity.setFileType(INMEMORYDB.ZIP);
            new AESCryptography().decryptionFile(jobEntity);
            
            /**Unzip File**/
            CommonUtils.unZipFile(jobEntity);
            
            //전송받은 진단스크립트 결과를 decryption 할떄 확장자를 .xml 로 하기 위하여.
            jobEntity.setFileType(INMEMORYDB.DGRESULTTYPE);
            
            //EUC-KR convert to UTF-8
            CommonUtils.convertUTF8(jobEntity);
            
            /**
             * 진단결과 파일 저장
             */
            try {
                customDiagnosisService.processingCustomDiagData(jobEntity);
                jobEntity.setAgentJobFlag(2);
				jobEntity.setAgentJobEDate(DateUtil.getCurrDateBySecond());
                jobEntity.setAgentJobDesc("SUCCESS");
                
            } catch (CustomDiagValidationException e) {
                jobEntity.setAgentJobFlag(3);
				jobEntity.setAgentJobEDate(DateUtil.getCurrDateBySecond());
                jobEntity.setAgentJobDesc(SnetCommonErrCode.ERR_0028.getMessage()  + " :: [ "+e.getMessage() +" ]");
                log.error(e.toString(), e);
            } catch (CustomDiagDataParseException e) {
                jobEntity.setAgentJobFlag(3);
				jobEntity.setAgentJobEDate(DateUtil.getCurrDateBySecond());
                jobEntity.setAgentJobDesc(SnetCommonErrCode.ERR_0029.getMessage() + " :: [ "+e.getMessage() +" ]");
                log.error(e.toString(), e);
            } catch (Exception e){
                jobEntity.setAgentJobFlag(3);
				jobEntity.setAgentJobEDate(DateUtil.getCurrDateBySecond());
                jobEntity.setAgentJobDesc(SnetCommonErrCode.ERR_0030.getMessage() + " :: [ "+e.getMessage() +" ]");
                log.error(e.toString(), e);
            }
            
            dao.updateEventAgentJobHistory(jobEntity);
            
            
            //진단결과 백업
            doDiagnosisResultBackup(jobEntity);
            
            //Delete GetScript result File.
            INMEMORYDB.deleteFile(jobEntity, INMEMORYDB.RECV);
            
            //kill agent
            if (jobEntity.isKillAgent()) {
                killAgent(jobEntity);
            }
            
            log.debug("=======Event Diagnosis REC_DIAG_Result FINISH=========");
            
        } catch (Exception e) {
        	log.error(e.toString(), e);
            throw new SnetException(e.getMessage());
        }
    }

    public void recvSRVManualDGscriptResult(JobEntity jobEntity) throws SnetException, IOException {

        try {
            //EUC-KR convert to UTF-8
            CommonUtils.convertUTF8(jobEntity);

            /**진단 결과파일 파싱*/
            dataParseManager.dscriptResult(jobEntity);

            //진단결과 백업
            doDiagnosisResultBackup(jobEntity);

            //Delete GetScript result File.
            INMEMORYDB.deleteFile(jobEntity, INMEMORYDB.RECV);

            jobEntity.setAgentJobFlag(3);
            jobEntity.setAgentJobEDate(DateUtil.getCurrDateBySecond());

            if(jobEntity.getJobType().equals(ManagerJobType.WM303.toString()))
                jobEntity.setAgentJobDesc("");
            else
                jobEntity.setAgentJobDesc("수동업로드 진단완료");

            if (jobEntity.getJobType().equals(ManagerJobType.WM202.toString()) || jobEntity.getJobType().equals(ManagerJobType.WM303.toString())) {
                dao.updateAgentJobHistory(jobEntity);
            }

            log.info("SRVManual Diagsnosis is done.");

//        } catch (MalformedByteSequenceException e) {
//            log.error(CommonUtils.printError(e));
//            throw new SnetException(SnetCommonErrCode.ERR_0015.getMessage());
        } catch (IOException e) {
            log.error(e.toString(), e); //  log.error(CommonUtils.printError(e));
            throw new IOException(SnetCommonErrCode.ERR_0035.getMessage());
        } catch (ParserConfigurationException e) {
        	log.error(e.toString(), e); // log.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0015.getMessage());
        } catch (SAXException e) {
        	log.error(e.toString(), e); // log.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0015.getMessage());
        } catch (SnetException e) {
        	log.error(e.toString(), e); // log.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0014.getMessage());
        } catch (InterruptedException e) {
        	log.error(e.toString(), e); // log.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0015.getMessage());
        } catch (Exception e) {
        	log.error(e.toString(), e); // log.error(CommonUtils.printError(e));
            throw new SnetException(e.getMessage());
        }

    }

    public void recvDGscriptResult(JobEntity jobEntity) throws Exception {

        log.debug("Send DGFIN and Receive DG Script result file");
        try {

            File[] f = new File[0];

            // deprecated (AES256 고려안함)
            // des 파일만 전송
            // /**Unzip File**/
            // CommonUtils.unZipFile(jobEntity);
            // Delete Zip File
            // CommonUtils.deleteZipFile(jobEntity);

            // deprecated
            // Check hash
            // CommonUtils.isValidHash(jobEntity);

            //Receive Zip File
            jobEntity.setFileType(INMEMORYDB.ZIP);
            new AESCryptography().decryptionFile(jobEntity);

            /**Unzip File**/
            CommonUtils.unZipFile(jobEntity);

            //전송받은 진단스크립트 결과를 decryption 할떄 확장자를 .xml 로 하기 위하여.
            jobEntity.setFileType(INMEMORYDB.DGRESULTTYPE);

            //EUC-KR convert to UTF-8
            CommonUtils.convertUTF8(jobEntity);

            /* 진단 실행 중 로그 */
            AgentLogUtil agentLogUtil = new AgentLogUtil();
            agentLogUtil.setLogDir(fileStorageProperties.getDiagnosisUploadDir());

            jobEntity.setStatusLog(agentLogUtil.analysisLogFile(jobEntity));
            log.debug(jobEntity.getStatusLog());

            /**
             * 진단결과 파일 파싱
             */
            dataParseManager.dscriptResult(jobEntity);


            //진단결과 백업
            doDiagnosisResultBackup(jobEntity);

            //Delete GetScript result File.
            INMEMORYDB.deleteFile(jobEntity, INMEMORYDB.RECV);

            //kill agent
            if (jobEntity.isKillAgent()) {
                killAgent(jobEntity);
            }

            jobEntity.setAgentJobDesc("SUCCESS");
            jobEntity.setAgentJobFlag(3);
            jobEntity.setAgentJobEDate(DateUtil.getCurrDateBySecond());
            log.debug("=======dignosisManager recvDGscriptResult DGFin=========");
            log.debug(jobEntity.toString());
            dao.updateAgentJobHistory(jobEntity);

        } catch (IOException e) {
            log.error(e.toString(), e);
            throw new IOException(SnetCommonErrCode.ERR_0033.getMessage());
        } catch (ParserConfigurationException e) {
        	log.error(e.toString(), e);
            throw new SnetException(SnetCommonErrCode.ERR_0015.getMessage());
        } catch (SAXException e) {
        	log.error(e.toString(), e);
            throw new SnetException(SnetCommonErrCode.ERR_0015.getMessage());
        } catch (SnetException e) {
			log.error(e.toString(), e);
			throw new SnetException(SnetCommonErrCode.ERR_0014.getMessage());
		} catch (ClassCastException e){
			log.error(e.toString(), e);
			throw new SnetException(SnetCommonErrCode.ERR_0015.getMessage());
		} catch (Exception e) {
        	log.error(e.toString(), e);
            throw new SnetException(e.getMessage());
		}
    }

    // 수집 / 진단 분리시 결과파일 처리 로직
    public void recvSRVDGscriptResult(JobEntity jobEntity) throws Exception {

        log.debug("Send SRVDGFIN and Receive SRVDG Script result file");
        try {

            jobEntity.setDiagInfoHandling(true);
            jobEntity.setFileType(INMEMORYDB.ZIP);
            new AESCryptography().decryptionFile(jobEntity);

            /**Unzip File**/
            CommonUtils.unSrvZipFile(jobEntity);

            // 수집 / 진단 분리시 사용된 진단용 환경파일 백업.
            doSrvDiagnosisResultBackup(jobEntity);
            JobEntity chk = dao.selectDiagInfo(jobEntity);
            if (chk != null && StringUtils.isNotEmpty(chk.getAssetCd())) {
                // 기존 백업파일 삭제
                CommonUtils.deleteFile(chk.getDiagInfoFilePath());
                CommonUtils.deleteFile(chk.getDiagInfoFilePathDes());

                jobEntity.setDiagInfoFlag(4);
                dao.updateDiagInfo(jobEntity);
            } else {
                dao.insertDiagInfo(jobEntity);
            }

            createDiagnosisForSvr_java14(jobEntity);

            /* 에이전트에서 수집된 로그 */
            String statusLog = "";
            AgentLogUtil agentLogUtil = new AgentLogUtil();
            agentLogUtil.setLogDir(jobEntity.getDiagDir() + File.separator);
            String getLog = agentLogUtil.analysisLogFile(jobEntity);

            DiagnosisUtil.handleDiagnosis(jobEntity);

            //전송받은 진단스크립트 결과를 decryption 할떄 확장자를 .xml 로 하기 위하여.
            jobEntity.setFileType(INMEMORYDB.DGRESULTTYPE);

            //EUC-KR convert to UTF-8
            CommonUtils.convertUTF8(jobEntity);

            /* 매니저서버에서 진단 실행 중 로그 */
            String diagLog = agentLogUtil.analysisLogFile(jobEntity);

            statusLog += "*******************************************************************************************************************************\n\n";
            statusLog += "*                                                 에이전트에서 수집 로그                                                      *\n\n";
            statusLog += "*******************************************************************************************************************************\n\n";
            statusLog += getLog;
            statusLog += "\n\n*******************************************************************************************************************************\n\n";
            statusLog += "*                                                 매니저에서 진단된 로그                                                      *\n\n";
            statusLog += "*******************************************************************************************************************************\n\n";
            statusLog += diagLog;

            jobEntity.setStatusLog(statusLog);

            log.debug("************** 에이전트에서 수집되면서 올라온 로그 *****************\n\n");
            log.debug(getLog);
            log.debug("************** 서버에서 진단되면서 올라온 로그 *****************\n\n");
            log.debug(diagLog);

            /**
             * 진단결과 파일 파싱
             */
            dataParseManager.dscriptResult(jobEntity);

            //Delete GetScript result File.
            INMEMORYDB.deleteFile(jobEntity, INMEMORYDB.RECV);

             // 수집 / 진단 분리시 사용된 진단용 해제된 환경파일 삭제.
             CommonUtils.deleteDirectory(new File(jobEntity.getDiagDir()));
             log.debug("delete files diaginfo files from " + jobEntity.getDiagDir());

            jobEntity.setAgentJobDesc("SUCCESS");
            jobEntity.setAgentJobFlag(3);
            jobEntity.setAgentJobEDate(DateUtil.getCurrDateBySecond());
            log.debug("=======dignosisManager recvSRVDGscriptResult DGFin=========");
            log.debug(jobEntity.toString());
            dao.updateAgentJobHistory(jobEntity);

            jobEntity.setDiagInfoHandling(false);
        } catch (Exception e) {
            // 수집 / 진단 분리시 사용된 진단용 해제된 환경파일 삭제.
            CommonUtils.deleteDirectory(new File(jobEntity.getDiagDir()));
            log.debug("delete files diaginfo files from " + jobEntity.getDiagDir());

            jobEntity.setDiagInfoHandling(false);

            log.error(e.toString(), e);
            throw new SnetException(e.getMessage());
        }
    }

    public void recvDGLastLog(JobEntity jobEntity) throws Exception {
        log.debug("Receive DG Last Log File From Agent");
        try {

            String uploadPath = fileStorageProperties.getUploadPath(ManagerJobType.AJ154.toString());

            CommonUtils.deleteFile(uploadPath + jobEntity.getAssetCd() + ".log");

            jobEntity.setFileType(INMEMORYDB.ZIP);
            jobEntity.setUploadLogOnly(true);
            jobEntity.setUploadLogOnlyPath(uploadPath);
            new AESCryptography().decryptionFile(jobEntity);

            /**Unzip File**/
            CommonUtils.unZipFile(jobEntity);

            /* 진단 실행 중 로그 */
            AgentLogUtil agentLogUtil = new AgentLogUtil();
            agentLogUtil.setLogDir(fileStorageProperties.getDoDiagLastLog());

            jobEntity.setStatusLog(agentLogUtil.analysisLogFile(jobEntity));
            log.debug(jobEntity.getStatusLog());

            CommonUtils.deleteFile(uploadPath + jobEntity.getFileName() + ".zip");
            CommonUtils.deleteFile(uploadPath + jobEntity.getFileName() + ".des");
            CommonUtils.moveFile(uploadPath + jobEntity.getFileName() + ".log", uploadPath + jobEntity.getAssetCd() + ".log");

            log.debug("=======dignosisManager recvDGLastLog=========");
            log.debug(jobEntity.toString());

            // 진단 실패로 인한 /process API (AN052)가 이미 호출 되었으므로 진단 실패 로그만 강제 업데이트
            RunnigJobEntity runnigJobEntity = INMEMORYDB.RUNNINGDGJOBLIST.get(jobEntity.getAssetCd());
            if (runnigJobEntity == null) {
                dao.updateLogFileAgentJobHistory(jobEntity);
            }

        } catch (Exception e) {
            log.error(e.toString(), e);
            throw new SnetException(e.getMessage());
        }
    }

    private void doDiagnosisResultBackup(JobEntity jobEntity) {
        String resultPath = INMEMORYDB.jobTypeAbsolutePath(INMEMORYDB.RECV,
                jobEntity.getJobType()) + jobEntity.getFileName() + jobEntity.getFileType();
        String movePath = INMEMORYDB.diagnoisResultAbsolutePath();

		String moveFile = jobEntity.getFileName()+"_"+jobEntity.getJobType()+"_"+DateUtil.getCurrDateBySecond()+jobEntity.getFileType();
//        String moveFile = jobEntity.getHostNm() + "_" + jobEntity.getAgentInfo().getConnectIpAddress() + "_" + DateUtil.getCurrDateBySecond() + jobEntity.getFileType();

        movePath += moveFile;

        CommonUtils.fileCopy(resultPath, movePath);
    }

    private void doSrvDiagnosisResultBackup(JobEntity jobEntity) {

        String resultPath = INMEMORYDB.jobTypeAbsolutePath(INMEMORYDB.RECV, jobEntity.getJobType()) + jobEntity.getFileName() + INMEMORYDB.DES;
        String diagInfoDnPath = INMEMORYDB.DIAG_INFO_FILE_TOMCAT_DIR + jobEntity.getFileName() + "_" + jobEntity.getAssetCd() + INMEMORYDB.ZIP;
        String movePath = INMEMORYDB.diagnoisResultAbsolutePath();
        String moveFile = jobEntity.getFileName() + "_" + jobEntity.getAssetCd() + INMEMORYDB.DES;

        movePath += moveFile;
        CommonUtils.fileCopy(resultPath, movePath);

        log.debug("resultPath : " + resultPath + ", movePath" + movePath);

        jobEntity.setDiagInfoFilePath(diagInfoDnPath);
        jobEntity.setDiagInfoFilePathDes(movePath);
    }

    public void killAgent(JobEntity jobEntity) throws Exception {

        File[] f = new File[0];

        SocketClient socket = new SocketClient(jobEntity);
        jobEntity.setJobType(ManagerJobFactory.KILLAGENT);
        socket.sendHeader(f, jobEntity);
        socket.closeSocket();
    }

    public boolean runProcessUP_bak(JobEntity jobEntity) throws Exception {
        boolean isJobDone = false;
        String inStream = "";

        try {
            log.debug("Run Process Send");


            AgentInfo ai = jobEntity.getAgentInfo();
            String accessType = ai.getChannelType();

            /**
             * channelType = ""; 'S' = 'SSH & SFTP', 'TF' = 'TELNET & FTP' , 'T' =
             * 'TELNET' only , 'F' = 'FTP' only
             **/

            log.debug("accessType :: " + accessType);


            String script = "";
            if (jobEntity.getJobType().equals(ManagerJobFactory.RELAYUP)) {
                script = INMEMORYDB.RUN_RELAY_SCRIPT;
            } else {
                script = INMEMORYDB.RUN_AGENT_SCRIPT;
            }

            String cmds = INMEMORYDB.absolutePath(script, jobEntity.getAgentInfo().getOsType());
            log.debug("cmds : " + cmds);

            //Send Execute Command VIA TELNET or SSH  && read Input And Save into A file.
            if (accessType.startsWith("S")) {

                inStream = connectionManager.runRemoteScriptViaSSH_Su(ai, cmds);
                log.debug(inStream);

                isJobDone = !(inStream.contains("[CONNECTION_FAILURE]") || inStream.contains("[COMMAND_FAILURE]"));

            } else if (accessType.startsWith("T")) {
                inStream = connectionManager.runRemoteScriptViaTelnet_Su(ai, ai.getUserIdRoot(), ai.getPasswordRoot(), cmds);
                log.debug(inStream);
                isJobDone = !(inStream.contains("FAILURE"));

            } else {
                log.debug("CANNOT CONNECT TELNET SERVICE.");
            }

            if (!isJobDone) {
                throw new Exception(inStream);
            }

            //TODO: save to File inStream

            return isJobDone;
        } catch (Exception e) {
        	log.error(e.toString(), e); // log.error(CommonUtils.printError(e));
            throw new Exception("접속이 실패하였습니다.(접속정보를 확인해 주세요.) 사유 : " + inStream);
//			throw new Exception("AgentUP Fail, socket is not established");
        }
    }


    // 3단계 페이지에서 진단기준, param1, param2. param3 추가
    private String makeConfig(JobEntity jobEntity) {
        String dbPwPath = INMEMORYDB.jobTypeAbsolutePath(INMEMORYDB.SEND, jobEntity.getJobType());
        try {
            FileWriter fw = null;
            BufferedWriter bw = null;
            fw = new FileWriter(dbPwPath + jobEntity.getSOTP() + jobEntity.getPassWordType());
            bw = new BufferedWriter(fw);

            String gov = "1";
            if (!StringUtils.isEmpty(jobEntity.getAuditFileType()) && jobEntity.getAuditFileType().equals("2") ) {
                gov = "msit";
            }

            // 2018.03.12 - #600. [매니져]rev. 1353 : 에이전트에서 사용할 소프트웨어 이름, 소프트웨어 경로를 포함한 dat2 파일을 전송하도록 수정
            //bw.write(jobEntity.getAgentInfo().getUserId() + INMEMORYDB.DELIMITER + jobEntity.getAgentInfo().getPassword() + INMEMORYDB.DELIMITER + jobEntity.getSwEtc() + INMEMORYDB.DELIMITER + jobEntity.getSwUser() + INMEMORYDB.DELIMITER + jobEntity.getSwDir() + INMEMORYDB.DELIMITER + jobEntity.getAuditSpeed() + INMEMORYDB.DELIMITER + jobEntity.getGlobalPwl() + INMEMORYDB.DELIMITER + jobEntity.getGlobalSts() + INMEMORYDB.DELIMITER + jobEntity.getAgentInfo().getSocketFilePath());
            //log.debug("[" + jobEntity.getAssetCd() + "] dgType = " + jobEntity.getSwType() + ", Make diag conf File : " + jobEntity.getAgentInfo().getUserId() + INMEMORYDB.DELIMITER + jobEntity.getAgentInfo().getPassword() + INMEMORYDB.DELIMITER + jobEntity.getSwEtc() + INMEMORYDB.DELIMITER + jobEntity.getSwUser() + INMEMORYDB.DELIMITER + jobEntity.getSwDir() + INMEMORYDB.DELIMITER + jobEntity.getAuditSpeed() + INMEMORYDB.DELIMITER + jobEntity.getGlobalPwl() + INMEMORYDB.DELIMITER + jobEntity.getGlobalSts() + INMEMORYDB.DELIMITER + jobEntity.getAgentInfo().getSocketFilePath());
            if (jobEntity.getAgentInfo().getOsType().toUpperCase().contains(INMEMORYDB.WIN_ID)) {
                String passwd = jobEntity.getAgentInfo().getPassword();
                if ("MSSQL".equalsIgnoreCase(jobEntity.getSwNm())) {
                    passwd = passwd.replaceAll("\\^", "^^^^").replaceAll("&", "^^^&")
                            .replaceAll("<", "^^^<").replaceAll(">", "^^^>").replaceAll("[|]", "^^^|");
                    jobEntity.getAgentInfo().setPassword(passwd);
                }else{
                    passwd = passwd.replaceAll("\\^", "^^").replaceAll("&", "^&")
                            .replaceAll("<", "^<").replaceAll(">", "^>").replaceAll("[|]", "^|");
                    jobEntity.getAgentInfo().setPassword(passwd);
                }
                String text = jobEntity.getAgentInfo().getUserId() + System.lineSeparator()
                        + jobEntity.getAgentInfo().getPassword() + System.lineSeparator()
                        + jobEntity.getSwEtc() + System.lineSeparator()
                        + jobEntity.getSwUser() + System.lineSeparator()
                        + jobEntity.getSwDir() + System.lineSeparator()
                        + jobEntity.getAuditSpeed() + System.lineSeparator()
                        + jobEntity.getGlobalPwl() + System.lineSeparator()
                        + jobEntity.getGlobalSts() + System.lineSeparator()
                        + jobEntity.getSwNm() + System.lineSeparator()
                        + jobEntity.getSwInfo() + System.lineSeparator()
                        + jobEntity.getAgentInfo().getSocketFilePath() + System.lineSeparator()
                        + gov + System.lineSeparator()
                        + jobEntity.getAgentInfo().getParam1() + System.lineSeparator()
                        + jobEntity.getAgentInfo().getParam2() + System.lineSeparator()
                        + jobEntity.getAgentInfo().getParam3();
                bw.write(text);

                if (jobEntity.getDiagInfoUse().toLowerCase().equals("y") && !INMEMORYDB.diagInfoNotUse) {
                    text += System.lineSeparator() + jobEntity.getDiagDir() + System.lineSeparator();

                    String dir = INMEMORYDB.jobTypeAbsolutePath(INMEMORYDB.RECV, jobEntity.getJobType());
                    String filePath = dir.concat(jobEntity.getFileName());

                    text += filePath + System.lineSeparator();
                }
                log.debug("[" + jobEntity.getAssetCd() + "] dgType = " + jobEntity.getSwType() + ", Make diag conf File : " + text);
            }else {
                String text = jobEntity.getAgentInfo().getUserId() + INMEMORYDB.DELIMITER
                        + jobEntity.getAgentInfo().getPassword() + INMEMORYDB.DELIMITER
                        + jobEntity.getSwEtc() + INMEMORYDB.DELIMITER
                        + jobEntity.getSwUser() + INMEMORYDB.DELIMITER
                        + jobEntity.getSwDir() + INMEMORYDB.DELIMITER
                        + jobEntity.getAuditSpeed() + INMEMORYDB.DELIMITER
                        + jobEntity.getGlobalPwl() + INMEMORYDB.DELIMITER
                        + jobEntity.getGlobalSts() + INMEMORYDB.DELIMITER
                        + jobEntity.getSwNm() + INMEMORYDB.DELIMITER
                        + jobEntity.getSwInfo() + INMEMORYDB.DELIMITER
                        + jobEntity.getAgentInfo().getSocketFilePath() + INMEMORYDB.DELIMITER
                        + gov + INMEMORYDB.DELIMITER
                        + jobEntity.getAgentInfo().getParam1() + INMEMORYDB.DELIMITER
                        + jobEntity.getAgentInfo().getParam2() + INMEMORYDB.DELIMITER
                        + jobEntity.getAgentInfo().getParam3();

                if (jobEntity.getDiagInfoUse().toLowerCase().equals("y") && !INMEMORYDB.diagInfoNotUse) {
                    text += INMEMORYDB.DELIMITER + jobEntity.getDiagDir() + INMEMORYDB.DELIMITER;

                    String dir = INMEMORYDB.jobTypeAbsolutePath(INMEMORYDB.RECV, jobEntity.getJobType());
                    String filePath = dir.concat(jobEntity.getFileName());

                    text += filePath + INMEMORYDB.DELIMITER;
                }
                bw.write(text);
                log.debug("[" + jobEntity.getAssetCd() + "] dgType = " + jobEntity.getSwType() + ", Make diag conf File : " + text);
            }


            if (bw != null) bw.close();
            if (fw != null) fw.close();

            return dbPwPath + jobEntity.getSOTP() + jobEntity.getPassWordType();
        } catch (IOException e) {
        	log.error(e.toString(), e); // log.error(CommonUtils.printError(e));
            INMEMORYDB.removeDGJOBList(jobEntity);
            return "fail";
        }
    }


    @SuppressWarnings({ "unused", "resource" })
	public static void main(String args[]) throws Exception {

        ApplicationContext ctx = new ClassPathXmlApplicationContext(
                "classpath:applicationContext.xml");

        JobEntity jobEntity = new JobEntity();
        AgentInfo agentInfo = new AgentInfo();
        agentInfo.setOsType("Linux");

        jobEntity.setAgentInfo(agentInfo);
        jobEntity.setJobType(ManagerJobFactory.DGFILE);
        jobEntity.setAuditFileName("skt_linux.sh");
        jobEntity.setSOTP("465431");
        jobEntity.setFileType(INMEMORYDB.SH);
        jobEntity.setSwNm("OS");
        jobEntity.setSwType("OS");

        // new DiagnosisManager().createDiagnosis_java14(jobEntity);

    }

    private String makeDiagnosisOptionConfig(JobEntity jobEntity) {

        String dbPwPath = INMEMORYDB.jobTypeAbsolutePath(INMEMORYDB.SEND, jobEntity.getJobType());
        try {
            List<ConfigAuditItemOption> configAuditItemOptionList = dao.selectConfigAuditItemOption(jobEntity);

            // 진단 제외 항목 있는지 검사.
            Map<String, List<ConfigAuditItemOption>> configAuditItemOptionMap = null;
            List<Map.Entry<String, List<ConfigAuditItemOption>>> mapToList = null;

            if (configAuditItemOptionList != null && configAuditItemOptionList.size() > 0) {
                configAuditItemOptionMap = configAuditItemOptionList.stream()
                        .collect(Collectors.groupingBy(ConfigAuditItemOption::getDiagnosisCd));

                mapToList = configAuditItemOptionMap.entrySet().stream().collect(Collectors.toList());
            }

            if (mapToList == null) {
                return "fail";
            }
            FileWriter fw = null;
            BufferedWriter bw = null;
            fw = new FileWriter(dbPwPath + jobEntity.getSOTP() + jobEntity.getPassWordOptionType());
            bw = new BufferedWriter(fw);

            StringBuilder text = new StringBuilder();
            for(int i=(mapToList.size()-1); i >= 0; i--) {
                String diagnosisCd = mapToList.get(i).getKey();
                text.append(diagnosisCd);
                text.append("=");
                List<ConfigAuditItemOption> list = mapToList.get(i).getValue();
                for (int j=0; j < list.size(); j++) {
                    ConfigAuditItemOption item = list.get(j);
                    text.append(item.getOptionKey());
                    text.append("^");
                    if (StringUtils.isEmpty(item.getOptionValue())) {
                        text.append(StringUtils.defaultString(item.getOptionDefaultValue(), "-"));
                    } else {
                        text.append(item.getOptionValue());
                    }
                    if (j < (list.size() - 1)) {
                        text.append(",");
                    }
                }
                text.append(System.lineSeparator());
            }
            //Set keyset = configAuditItemOptionMap.keySet();
            //for(Iterator iter = keyset.iterator(); iter.hasNext();) {
//            for(int i=0; i < mapToList.size(); i++) {
//                text.append(diagnosisCd);
//                text.append("=");
//
//                List<ConfigAuditItemOption> configAuditItemOptions = configAuditItemOptionMap.get(diagnosisCd);
//                for(int j=0; j < configAuditItemOptions.size(); j++) {
//                    //                    ConfigAuditItemOption item = configAuditItemOptions.get(j);
////                    text.append(item.getKeyId());
////                    text.append("^");
////                    text.append(item.getVal());
////                    if (ij< (configAuditItemOptions.size() - 1)) {
////                        text.append(",");
//                    }
//                }
//
//                text.append(System.lineSeparator());
//            }

            bw.write(text.toString());

            if (bw != null) bw.close();
            if (fw != null) fw.close();

            log.debug("makeDiagnosisOptionConfig : " + text);

            return dbPwPath + jobEntity.getSOTP() + jobEntity.getPassWordOptionType();

        } catch (Exception e) {

            log.error(e.toString(), e);
            INMEMORYDB.removeDGJOBList(jobEntity);
            return "fail";
        }
    }
}