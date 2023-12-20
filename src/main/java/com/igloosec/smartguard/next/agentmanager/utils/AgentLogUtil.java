package com.igloosec.smartguard.next.agentmanager.utils;

import com.igloosec.smartguard.next.agentmanager.entity.JobEntity;
import com.igloosec.smartguard.next.agentmanager.memory.INMEMORYDB;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

@Slf4j
@Data
public class AgentLogUtil {

    private String LogDir;

    public String analysisLogFile(JobEntity jobEntity) throws Exception {
        if (LogDir != null && !LogDir.isEmpty()) {
            String dir = INMEMORYDB.jobTypeAbsolutePath(LogDir);
            String logFilePath = dir + jobEntity.getFileName() + ".log";
            log.debug("Diagnosis Log File : " + logFilePath);

            File logFile = new File(logFilePath);

            if (logFile.exists()) {
                BufferedReader logFileBr = new BufferedReader(new InputStreamReader(new FileInputStream(logFile), "utf-8"));
                StringBuffer logFileText = new StringBuffer();

                String line = "";
                while ((line = logFileBr.readLine()) != null) {
                    logFileText.append(line.replaceAll("\r", "").replaceAll("\n", "") + "\n");
                }

                if (logFileBr != null) {
                    logFileBr.close();
                }
                jobEntity.setStatusLog(logFileText.toString());
            } else {
                log.debug("Not Log File!!");
                jobEntity.setStatusLog("Log File does not exist.");
            }
        } else {
            log.debug("LogDir is not set!!");
        }

        return jobEntity.getStatusLog();
    }
}
