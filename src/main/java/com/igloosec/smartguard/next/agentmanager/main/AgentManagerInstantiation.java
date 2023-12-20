/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.services.AgentManagerInstantiation.java
 * company : Mobigen
 *
 * @author : Je Joong Lee
 * created at : 2016. 3. 24.
 * description :
 */

package com.igloosec.smartguard.next.agentmanager.main;

import com.igloosec.smartguard.next.agentmanager.memory.INMEMORYDB;
import com.igloosec.smartguard.next.agentmanager.property.AgentContextProperties;
import com.igloosec.smartguard.next.agentmanager.services.*;
import com.igloosec.smartguard.next.agentmanager.utils.DateUtil;

import com.sk.snet.manipulates.EncryptUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;

@Slf4j
@Component
public class AgentManagerInstantiation {

    private NotificationListener notificationListener;
    private ProcessMontierManager processMontierManager;
    private AgentContextProperties agentContextProperties;
    private AgentJobScheduleManager agentJobScheduleManager;
    private InitConfigMemory initConfigMemory;
    private AgentVersionManager agentVersionManager;
    private AgentResourceManager agentResourceManager;

    public AgentManagerInstantiation(NotificationListener notificationListener, ProcessMontierManager processMontierManager,
                                     AgentContextProperties agentContextProperties, AgentJobScheduleManager agentJobScheduleManager,
                                     InitConfigMemory initConfigMemory, AgentVersionManager agentVersionManager,
                                     AgentResourceManager agentResourceManager) {

        this.notificationListener = notificationListener;
        this.processMontierManager = processMontierManager;
        this.agentContextProperties = agentContextProperties;
        this.agentJobScheduleManager = agentJobScheduleManager;
        this.initConfigMemory = initConfigMemory;
        this.agentVersionManager = agentVersionManager;
        this.agentResourceManager = agentResourceManager;

    }

    /**
     * 초기화해야할 것들..
     */

    @PostConstruct
    public void init() {

        INMEMORYDB memory = new INMEMORYDB();
        memory.init();
        memory.initManagerDirs();
        memory.initPtyPhrases();
        INMEMORYDB.serverStartedAt = DateUtil.getCurrDateBySecondFmt();
        INMEMORYDB.latestAgentVersion = agentVersionManager.getAgentLatestVersionStr();
        agentResourceManager.checkCurrentAgentManagerVersion();
        log.debug("Init AgentManager ....");

        initConfigMemory.initMemory();
        initConfigMemory.resetProperties();
        setMemoryDB();
        notificationListener.getGSWorker();
        notificationListener.getNWWorker();
        notificationListener.agentInstallWorker();
        // 최종적으로 properties에 셋팅 값으로 memorydb data 변경.

        agentJobScheduleManager.initAgentJobScheduleList();
        Field[] field = INMEMORYDB.class.getFields();
        try {
            System.out.println("=========================[[ Init Memory DB ]]=========================");
            for (Field f : field) {
                if (f.getType().getTypeName().equals("java.lang.String")
                        || f.getType().getTypeName().equals("int")
                        || f.getType().getTypeName().equals("boolean")) {
                    log.debug("{}={}", f.getName(), f.get(memory));
                }
            }
            System.out.println("=========================[[ Init Memory DB ]]=========================");
        } catch (Exception e) {
            e.printStackTrace();
        }

        /** Job Timeout work **/
        processMontierManager.doProcessMontier();
    }

    @SuppressWarnings("unused")
	private void makePropertieFile() {
        INMEMORYDB memory = new INMEMORYDB();
        Field[] field = INMEMORYDB.class.getFields();
        try {
            String fileName = "agent.context.properites";
            // 파일 객체 생성
            File file = new File(fileName);

            if (!file.isFile()) {
                // BufferedWriter 와 FileWriter를 조합하여 사용 (속도 향상)
                BufferedWriter fw = new BufferedWriter(new FileWriter(fileName, true));

                log.debug("===============================[[ INMEMORYDB ]]===============================");
                for (Field f : field) {
                    if (f.getType().getTypeName().equals("java.lang.String")
                            || f.getType().getTypeName().equals("int")
                            || f.getType().getTypeName().equals("boolean")) {

                        String txt = f.getName() + "=" + replacer(f.get(memory).toString()) + "\n";
                        log.debug("{}={}", f.getName(), replacer(f.get(memory).toString()));
                        fw.write(txt);
                    }
                }
                log.debug("===============================[[ INMEMORYDB ]]===============================");
                // 파일안에 문자열 쓰기
                fw.flush();

                log.debug("INMEMORYDB Propertie file path :: {}", file.getAbsolutePath());
                // 객체 닫기
                fw.close();
            } else {
                log.debug("INMEMORYDB Propertie file already existed!! :: {}", file.getAbsolutePath());
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }

    private String replacer(String str) {
        String result = "";
        if (str != null)
            result = str.replace("\\", "\\\\");
        return result;
    }

    private void setMemoryDB() {
        if (StringUtils.isNotEmpty(agentContextProperties.getJobDGTimeOut())) {
            INMEMORYDB.JobDGTimeOut = Integer.parseInt(agentContextProperties.getJobDGTimeOut());
        }
        if (StringUtils.isNotEmpty(agentContextProperties.getJobGSTimeOut())) {
            INMEMORYDB.JobGSTimeOut = Integer.parseInt(agentContextProperties.getJobGSTimeOut());
        }
        if (StringUtils.isNotEmpty(agentContextProperties.getJobNWTimeOut())) {
            INMEMORYDB.JobNWTimeOut = Integer.parseInt(agentContextProperties.getJobNWTimeOut());
        }
        if (StringUtils.isNotEmpty(agentContextProperties.getJobSetupTimeOut())) {
            INMEMORYDB.JobSetupTimeOut = Integer.parseInt(agentContextProperties.getJobSetupTimeOut());
        }
        if (StringUtils.isNotEmpty(agentContextProperties.getJobControlTimeOut())) {
            INMEMORYDB.JobControlTimeOut = Integer.parseInt(agentContextProperties.getJobControlTimeOut());
        }
        if (StringUtils.isNotEmpty(agentContextProperties.getJobApiTimeOut())) {
            INMEMORYDB.JobApiTimeOut = Integer.parseInt(agentContextProperties.getJobApiTimeOut());
        }
        if (StringUtils.isNotEmpty(agentContextProperties.getJobLogTimeOut())) {
            INMEMORYDB.JobLogTimeOut = Integer.parseInt(agentContextProperties.getJobLogTimeOut());
        }

        if (StringUtils.isNotEmpty(agentContextProperties.getMultiServices()) && agentContextProperties.getMultiServices().toUpperCase().equals("Y")) {
            INMEMORYDB.MultiServices = true;
        }

        if (StringUtils.isNotEmpty(agentContextProperties.getDiagExcept()) && agentContextProperties.getDiagExcept().toUpperCase().equals("Y")) {
            INMEMORYDB.DiagExcept = true;
        }

        if (StringUtils.isNotEmpty(agentContextProperties.getUseNotification()) && agentContextProperties.getUseNotification().toUpperCase().equals("Y")) {
            INMEMORYDB.useNotification = true;
        }
    }
}
