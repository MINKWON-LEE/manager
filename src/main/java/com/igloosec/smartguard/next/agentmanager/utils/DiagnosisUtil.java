package com.igloosec.smartguard.next.agentmanager.utils;

import com.igloosec.smartguard.next.agentmanager.entity.JobEntity;
import com.igloosec.smartguard.next.agentmanager.exception.SnetException;
import com.igloosec.smartguard.next.agentmanager.memory.INMEMORYDB;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.*;

@Slf4j
public class DiagnosisUtil {

    static final int waitTime = 60 * 1000 * Integer.parseInt(INMEMORYDB.EXEC_WAIT_TIME);

    public static void handleDiagnosis(JobEntity jobEntity) throws Exception {

        CommandLine cmdLine;

        String workDir =  INMEMORYDB.jobTypeAbsolutePath(INMEMORYDB.SEND, jobEntity.getJobType());

        String filePath = workDir.concat(jobEntity.getSOTP());
        String dgFilePath = filePath + "_svr" +  INMEMORYDB.SH;
        String gsFilePath = jobEntity.getSOTP() + jobEntity.getGetForDiag() + INMEMORYDB.SH;
        String dbPwPath = jobEntity.getSOTP() + jobEntity.getPassWordType();

        File file = new File(jobEntity.getDiagDir());
        if (!file.exists() && !file.isDirectory()) {
            String msg = "uploaded file do not exist from diagnosis path ::" + filePath;
            log.debug(msg);
            throw new Exception(msg);
        }

        cmdLine = CommandLine.parse("sudo /bin/sh");
        cmdLine.addArgument("-c");

        String exec = "LANG=C;export LANG;"+ "cd "+ workDir + ";" + "chmod 755 ./*; sudo sh "+ dgFilePath + " " + gsFilePath + " " + dbPwPath;
        log.debug("진단 프로그램 실행 : " + exec);
        cmdLine.addArgument(exec, false);

        DefaultExecutor executor = new DefaultExecutor();
        ExecuteWatchdog watchdog = new ExecuteWatchdog(waitTime);
        executor.setWatchdog(watchdog);
        ByteArrayOutputStream outputStreamStart = new ByteArrayOutputStream();
        PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(outputStreamStart);
        pumpStreamHandler.setStopTimeout(waitTime);
        executor.setStreamHandler(pumpStreamHandler);

        int exitCode = -1;
        //진단로그 파일 생성
        BufferedWriter diagLogFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(jobEntity.getDiagDir() + File.separator +jobEntity.getFileName() + ".log"), "utf-8"));

        try {
            long startTime = System.currentTimeMillis();
            exitCode = executor.execute(cmdLine);

            long duration = System.currentTimeMillis() - startTime;
            log.debug("Process completed in " + duration + " millis; below is its output");
            log.debug("exit " + exitCode);

            diagLogFile.write("Process completed in " + duration + " millis; below is its output ");
            diagLogFile.newLine();
            diagLogFile.write("Server Diagnosis Success !!! ");
            diagLogFile.newLine();

        } catch (IOException e) {
            log.error("Server Diagnosis Error !!! " + e.getMessage());
            outputStreamStart.close();
            diagLogFile.close();
            throw new Exception(e);
        } finally {
            diagLogFile.write(new String(outputStreamStart.toByteArray(),"utf-8"));
            outputStreamStart.close();
            diagLogFile.close();
        }

        if (watchdog.killedProcess()) {
            // to do 스크립트 중지 로직 필요.
            log.error("Process timed out and was killed by watchdog.");
        }

        log.debug("Server Diagnosis Success !!! ");

        String osType = System.getProperty("os.name").toLowerCase();
        if (!osType.contains("win")) {
            String diagPath = INMEMORYDB.jobTypeAbsolutePath(INMEMORYDB.RECV, jobEntity.getJobType());
            String cmd = "sudo chown sgweb:sgweb " + diagPath + jobEntity.getFileName();
            Runtime.getRuntime().exec(cmd + INMEMORYDB.DGRESULTTYPE);
            Runtime.getRuntime().exec(cmd + INMEMORYDB.LOG);
            log.debug(cmd);
        }

        Thread.sleep(500);
    }

    public static void moveDiagResultFile(JobEntity jobEntity) {
        String retFile = jobEntity.getDiagDir() + File.separator +  jobEntity.getSOTP() + INMEMORYDB.DGRESULTTYPE;
        String diagPath = INMEMORYDB.jobTypeAbsolutePath(INMEMORYDB.RECV, jobEntity.getJobType());

        CommonUtils.moveFile(retFile, diagPath);

        log.debug("moved diag result File. from : " + retFile + ", to :" + diagPath);
    }
}
